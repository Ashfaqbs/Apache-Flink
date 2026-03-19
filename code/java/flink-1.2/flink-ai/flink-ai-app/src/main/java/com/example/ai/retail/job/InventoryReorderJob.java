package com.example.ai.retail.job;

import com.example.ai.retail.agent.ReorderAgent;
import com.example.ai.retail.agent.SalesVelocityAgent;
import com.example.ai.retail.dto.ReorderRecommendation;
import com.example.ai.retail.dto.SkuVelocityResult;
import com.example.ai.retail.dto.SkuWindowSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.agents.api.AgentsExecutionEnvironment;
import org.apache.flink.agents.api.agents.AgentExecutionOptions;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;
import org.apache.flink.agents.api.resource.ResourceType;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Two-stage Flink Agents pipeline for automated inventory reorder recommendations.
 *
 * Reads sale events from Kafka, assesses per-SKU stock velocity via Agent 1,
 * aggregates velocity signals across a 5-minute window, then generates a restock
 * recommendation with urgency and suggested order quantity via Agent 2.
 *
 * Pipeline overview:
 *
 *   [Kafka: sale-events]
 *          |
 *          | (SaleEvent JSON deserialized per record)
 *          v
 *   [Agent 1: SalesVelocityAgent — Groq LLM]
 *          |  - Classifies velocity: HIGH / MEDIUM / LOW
 *          |  - Estimates days to stockout
 *          |  - Identifies stock concerns
 *          |  - Emits SkuVelocityResult keyed by skuId
 *          v
 *   [Tumbling Window: 5 minutes, keyed by skuId]
 *          |  - Counts sale events (proxy for total units)
 *          |  - Averages estimated days to stockout
 *          |  - Collects all stock concerns
 *          |  - Serializes to SkuWindowSummary JSON
 *          v
 *   [Agent 2: ReorderAgent — Groq LLM]
 *          |  - Generates CRITICAL/HIGH/MEDIUM/LOW urgency
 *          |  - Suggests order quantity
 *          |  - If CRITICAL or HIGH:
 *          |      -> calls notifyProcurement tool
 *          |      -> calls sendRestockAlert tool
 *          v
 *   [Kafka: reorder-recommendations]
 *          |
 *          v
 *   (Downstream: ERP/WMS system, procurement dashboard, warehouse alerts)
 */
public class InventoryReorderJob {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Window function that aggregates all SkuVelocityResults for a single SKU
     * within a 5-minute tumbling window into a SkuWindowSummary for Agent 2.
     *
     * Note: SkuVelocityResult does not carry raw quantity or current stock — that
     * information was consumed by Agent 1 to compute the velocity assessment.
     * We use element count as a proxy for activity volume and set minStockSeen=0
     * since exact stock figures are not available post-Agent-1.
     */
    static class AggregateSkuWindow
            extends ProcessWindowFunction<SkuVelocityResult, String, String, TimeWindow> {

        @Override
        public void process(
                String skuId,
                Context context,
                Iterable<SkuVelocityResult> elements,
                Collector<String> out) throws JsonProcessingException {

            int count = 0;
            long totalDaysToStockout = 0;
            String productName = "";
            // warehouseId is not in SkuVelocityResult — it's stored in memory by ReorderAgent
            // We pass an empty string here; Agent 2 will read warehouseId from short-term memory
            String warehouseId = "";
            List<String> allConcerns = new ArrayList<>();

            for (SkuVelocityResult result : elements) {
                count++;
                totalDaysToStockout += result.getEstimatedDaysToStockout();
                // Capture productName from the first element — consistent within a SKU window
                if (productName.isEmpty() && result.getProductName() != null) {
                    productName = result.getProductName();
                }
                // Collect all concerns — duplicates are intentional (frequency signals urgency)
                if (result.getStockConcerns() != null) {
                    allConcerns.addAll(result.getStockConcerns());
                }
            }

            // Average days to stockout across all velocity assessments in the window
            double avgDaysToStockout = count > 0 ? (double) totalDaysToStockout / count : 0.0;

            // totalUnitsSold uses count as a proxy (each event = one sale batch)
            // minStockSeen is 0 because stock level is not carried through post-Agent-1
            SkuWindowSummary summary = new SkuWindowSummary(
                    skuId,
                    productName,
                    warehouseId,
                    count,
                    avgDaysToStockout,
                    0,
                    allConcerns);

            out.collect(MAPPER.writeValueAsString(summary));
        }
    }

    public static void main(String[] args) throws Exception {
        // Fail fast if the Groq API key is absent
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY environment variable is not set. " +
                    "Export it before submitting the job.");
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        AgentsExecutionEnvironment agentsEnv = AgentsExecutionEnvironment.getExecutionEnvironment(env);

        // Two agents share the same Groq connection — cap threads to avoid rate limiting
        agentsEnv.getConfig().set(AgentExecutionOptions.NUM_ASYNC_THREADS, 2);

        // Register Groq via the OpenAI-compatible endpoint
        ResourceDescriptor groqConn = ResourceDescriptor.Builder
                .newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", groqApiKey)
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);

        // Kafka source: sale events from POS or e-commerce platform
        KafkaSource<com.example.ai.retail.dto.SaleEvent> source =
                KafkaSource.<com.example.ai.retail.dto.SaleEvent>builder()
                        .setBootstrapServers("kafka:9093")
                        .setTopics("sale-events")
                        .setGroupId("inventory-reorder-group")
                        .setDeserializer(
                                KafkaRecordDeserializationSchema.valueOnly(
                                        new SaleEventDeserializationSchema()))
                        .build();

        DataStream<com.example.ai.retail.dto.SaleEvent> saleEventStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source: sale-events");

        // --- STAGE 1: Assess per-event SKU velocity ---
        DataStream<SkuVelocityResult> velocityStream = agentsEnv
                .fromDataStream(saleEventStream)
                .apply(new SalesVelocityAgent())
                .toDataStream()
                .map(obj -> (SkuVelocityResult) obj);

        // --- TUMBLING WINDOW: 5-minute aggregation per SKU ---
        // Aggregates velocity assessments to smooth out single-event noise
        DataStream<String> windowedStream = velocityStream
                .keyBy(SkuVelocityResult::getSkuId)
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(5)))
                .process(new AggregateSkuWindow())
                .name("sku-velocity-window-aggregation");

        // --- STAGE 2: Generate reorder recommendation from aggregated data ---
        DataStream<ReorderRecommendation> recommendationStream = agentsEnv
                .fromDataStream(windowedStream)
                .apply(new ReorderAgent())
                .toDataStream()
                .map(obj -> (ReorderRecommendation) obj);

        // Kafka sink: reorder recommendations consumed by procurement and WMS systems
        KafkaSink<ReorderRecommendation> reorderSink = KafkaSink.<ReorderRecommendation>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("reorder-recommendations")
                                .setValueSerializationSchema(new ReorderRecommendationSerializer())
                                .build())
                .build();

        recommendationStream
                .sinkTo(reorderSink)
                .name("Kafka Sink: reorder-recommendations");

        agentsEnv.execute();
    }
}
