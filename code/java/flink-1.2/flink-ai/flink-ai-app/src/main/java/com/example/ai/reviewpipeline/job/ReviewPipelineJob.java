package com.example.ai.reviewpipeline.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.flink.table.api.Schema;
import org.apache.flink.util.Collector;

import org.apache.flink.agents.api.AgentsExecutionEnvironment;
import org.apache.flink.agents.api.agents.AgentExecutionOptions;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;
import org.apache.flink.agents.api.resource.ResourceType;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import com.example.ai.reviewpipeline.agent.ProductSuggestionAgent;
import com.example.ai.reviewpipeline.agent.ReviewAnalysisAgent;
import com.example.ai.reviewpipeline.dto.ProductSuggestion;
import com.example.ai.reviewpipeline.dto.ReviewAnalysisResult;
import com.example.ai.reviewpipeline.dto.ReviewWindowSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Two-stage AI review pipeline using Flink Agents + Groq.
 *
 * Pipeline:
 *
 *   [File Source — input_data.txt]
 *         |
 *         | (Table API — structured rows with id + review columns)
 *         v
 *   [Agent 1: ReviewAnalysisAgent]  <-- Groq: extracts score (1-5) + reasons per review
 *         |                              Also calls notifyShippingManager tool if shipping
 *         |                              complaint is detected
 *         v
 *   [Tumbling Window — 1 minute, keyed by product id]
 *         |
 *         | Aggregates: score histogram (% of each star rating) + all reasons collected
 *         v
 *   [Agent 2: ProductSuggestionAgent]  <-- Groq: generates 3 improvement suggestions
 *         |                                  from the aggregated window data
 *         v
 *   [Kafka Sink — topic: product-suggestions]
 *         |
 *         v
 *   (Downstream consumers: dashboards, product teams, alerting systems)
 */
public class ReviewPipelineJob {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Window function: aggregates ReviewAnalysisResults within a tumbling window
     * into a score histogram and a flat list of all unsatisfied reasons, then
     * serializes to JSON for Agent 2.
     */
    static class AggregateReviewWindow
            extends ProcessWindowFunction<ReviewAnalysisResult, String, String, TimeWindow> {

        @Override
        public void process(
                String productId,
                Context context,
                Iterable<ReviewAnalysisResult> elements,
                Collector<String> out) throws JsonProcessingException {

            int[] ratingCounts = new int[5];
            List<String> allReasons = new ArrayList<>();

            for (ReviewAnalysisResult result : elements) {
                int score = result.getScore();
                if (score >= 1 && score <= 5) {
                    ratingCounts[score - 1]++;
                }
                allReasons.addAll(result.getReasons());
            }

            int total = 0;
            for (int count : ratingCounts) total += count;

            List<String> histogram = new ArrayList<>();
            for (int count : ratingCounts) {
                double pct = total > 0 ? Math.round((count * 100.0 / total) * 10.0) / 10.0 : 0.0;
                histogram.add(String.format("%.1f%%", pct));
            }

            ReviewWindowSummary summary = new ReviewWindowSummary(productId, histogram, allReasons);
            out.collect(MAPPER.writeValueAsString(summary));
        }
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // StreamTableEnvironment bridges the Table API (SQL-like structured reads)
        // with the DataStream API — needed to use the filesystem connector as a typed source.
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // AgentsExecutionEnvironment wraps both envs so agents can work with Table input.
        AgentsExecutionEnvironment agentsEnv =
                AgentsExecutionEnvironment.getExecutionEnvironment(env, tableEnv);

        // Limit concurrent LLM calls to avoid rate-limiting on Groq's free tier.
        agentsEnv.getConfig().set(AgentExecutionOptions.NUM_ASYNC_THREADS, 2);

        // Register Groq via the OpenAI-compatible connection.
        // API key is read from the environment variable GROQ_API_KEY.
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY environment variable is not set.");
        }

        ResourceDescriptor groqConn = ResourceDescriptor.Builder
                .newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", groqApiKey)
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);

        // Read input_data.txt from the classpath and expose it as a Flink Table.
        // Each line is a JSON object: {"id": "B001", "review": "..."}
        // The filesystem connector parses it into typed rows (id STRING, review STRING).
        File inputFile = copyResource("input_data.txt");
        tableEnv.createTemporaryTable(
                "product_reviews",
                TableDescriptor.forConnector("filesystem")
                        .schema(
                                Schema.newBuilder()
                                        .column("id", DataTypes.STRING())
                                        .column("review", DataTypes.STRING())
                                        .build())
                        .option("format", "json")
                        .option("path", inputFile.getAbsolutePath())
                        .build());

        Table reviewsTable = tableEnv.from("product_reviews");

        // --- STAGE 1: Review Analysis Agent ---
        // Each Row (id, review) is processed by the LLM to produce a score + reasons.
        // The RowKeySelector keys the agent stream by product id.
        DataStream<Object> analysisStream =
                agentsEnv
                        .fromTable(reviewsTable, new ReviewAnalysisAgent.RowKeySelector())
                        .apply(new ReviewAnalysisAgent())
                        .toDataStream();

        // --- TUMBLING WINDOW: 1-minute aggregation per product ---
        // Groups all analysis results for the same product id within each window,
        // building a score histogram and collecting all dissatisfaction reasons.
        DataStream<String> windowedStream =
                analysisStream
                        .map(obj -> (ReviewAnalysisResult) obj)
                        .keyBy(ReviewAnalysisResult::getId)
                        .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(1)))
                        .process(new AggregateReviewWindow())
                        .name("review-window-aggregation");

        // --- STAGE 2: Product Suggestion Agent ---
        // The aggregated window summary is sent to the LLM which generates
        // 3 actionable improvement suggestions per product.
        DataStream<Object> suggestionStream =
                agentsEnv
                        .fromDataStream(windowedStream)
                        .apply(new ProductSuggestionAgent())
                        .toDataStream();

        // --- KAFKA SINK: product-suggestions topic ---
        // Downstream systems (dashboards, product teams, alerting) consume from here.
        KafkaSink<ProductSuggestion> kafkaSink = KafkaSink.<ProductSuggestion>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("product-suggestions")
                                .setValueSerializationSchema(new ProductSuggestionSerializer())
                                .build())
                .build();

        suggestionStream
                .map(obj -> (ProductSuggestion) obj)
                .sinkTo(kafkaSink)
                .name("product-suggestions-kafka-sink");

        agentsEnv.execute();
    }

    /** Copies a classpath resource to a temp directory so Flink's filesystem connector can read it. */
    private static File copyResource(String resourceName) throws Exception {
        try (InputStream stream = ReviewPipelineJob.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new FileNotFoundException(resourceName + " not found on classpath");
            }
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "review-pipeline-" + System.nanoTime());
            if (!tmpDir.mkdirs()) {
                throw new IOException("Could not create temp dir: " + tmpDir.getAbsolutePath());
            }
            File target = new File(tmpDir, resourceName);
            Files.copy(stream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target;
        }
    }
}
