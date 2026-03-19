package com.example.ai.banking.job;

import com.example.ai.banking.agent.LoanRiskExtractionAgent;
import com.example.ai.banking.agent.LoanRiskReportAgent;
import com.example.ai.banking.dto.LoanApplication;
import com.example.ai.banking.dto.LoanRiskReport;
import com.example.ai.banking.dto.LoanRiskSignals;
import com.example.ai.banking.dto.LoanWindowSummary;
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
 * Two-stage Flink Agents pipeline for automated loan risk assessment.
 *
 * Reads loan applications from Kafka, extracts per-application risk signals via
 * Agent 1, aggregates signals across a 5-minute window per applicant, then
 * generates a final underwriting decision via Agent 2.
 *
 * Pipeline overview:
 *
 *   [Kafka: loan-applications]
 *          |
 *          | (LoanApplication JSON deserialized per record)
 *          v
 *   [Agent 1: LoanRiskExtractionAgent — Groq LLM]
 *          |  - Assigns risk score 1-10
 *          |  - Identifies risk factors (credit score, DTI ratio, employment)
 *          |  - Emits LoanRiskSignals keyed by applicantId
 *          v
 *   [Tumbling Window: 5 minutes, keyed by applicantId]
 *          |  - Averages risk scores
 *          |  - Collects all risk factors
 *          |  - Collects all per-application recommendations
 *          |  - Serializes to LoanWindowSummary JSON
 *          v
 *   [Agent 2: LoanRiskReportAgent — Groq LLM]
 *          |  - Generates final APPROVE / REVIEW / DECLINE decision
 *          |  - If REVIEW: calls notifyUnderwriter tool (human-in-the-loop)
 *          |  - Emits LoanRiskReport
 *          v
 *   [Kafka: loan-risk-reports]
 *          |
 *          v
 *   (Downstream: loan origination system, compliance, underwriter dashboard)
 */
public class LoanRiskPipelineJob {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Window function that aggregates all LoanRiskSignals for one applicant within
     * a 5-minute tumbling window into a single LoanWindowSummary for Agent 2.
     *
     * Averaging the risk score across multiple applications gives a more stable
     * signal than any single application — applicants sometimes submit multiple
     * applications with slightly different inputs to get a better rate.
     */
    static class AggregateLoanWindow
            extends ProcessWindowFunction<LoanRiskSignals, String, String, TimeWindow> {

        @Override
        public void process(
                String applicantId,
                Context context,
                Iterable<LoanRiskSignals> elements,
                Collector<String> out) throws JsonProcessingException {

            int totalRiskScore = 0;
            int count = 0;
            List<String> allRiskFactors = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            for (LoanRiskSignals signals : elements) {
                totalRiskScore += signals.getRiskScore();
                count++;
                // Union of all risk factors — duplicates are intentional (frequency matters)
                if (signals.getRiskFactors() != null) {
                    allRiskFactors.addAll(signals.getRiskFactors());
                }
                if (signals.getApprovalRecommendation() != null) {
                    recommendations.add(signals.getApprovalRecommendation());
                }
            }

            // Guard against empty windows (shouldn't happen with keyed streams but be safe)
            double avgRiskScore = count > 0 ? (double) totalRiskScore / count : 0.0;

            LoanWindowSummary summary = new LoanWindowSummary(
                    applicantId,
                    count,
                    avgRiskScore,
                    allRiskFactors,
                    recommendations);

            out.collect(MAPPER.writeValueAsString(summary));
        }
    }

    public static void main(String[] args) throws Exception {
        // Fail fast if the Groq API key is absent — prevents misleading runtime errors
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY environment variable is not set. " +
                    "Export it before submitting the job.");
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        AgentsExecutionEnvironment agentsEnv = AgentsExecutionEnvironment.getExecutionEnvironment(env);

        // Limit concurrent LLM calls — two agents share the same Groq connection
        agentsEnv.getConfig().set(AgentExecutionOptions.NUM_ASYNC_THREADS, 2);

        // Register Groq via the OpenAI-compatible endpoint
        ResourceDescriptor groqConn = ResourceDescriptor.Builder
                .newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", groqApiKey)
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);

        // Kafka source: loan applications submitted by customers
        KafkaSource<LoanApplication> source = KafkaSource.<LoanApplication>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("loan-applications")
                .setGroupId("loan-risk-group")
                .setDeserializer(
                        KafkaRecordDeserializationSchema.valueOnly(
                                new LoanApplicationDeserializationSchema()))
                .build();

        DataStream<LoanApplication> applicationStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source: loan-applications");

        // --- STAGE 1: Extract risk signals per application ---
        // Each LoanApplication is independently scored by the LLM
        DataStream<LoanRiskSignals> riskSignalsStream = agentsEnv
                .fromDataStream(applicationStream)
                .apply(new LoanRiskExtractionAgent())
                .toDataStream()
                .map(obj -> (LoanRiskSignals) obj);

        // --- TUMBLING WINDOW: 5-minute aggregation per applicant ---
        // Groups all risk signals for the same applicant, computes average risk score,
        // and collects all factors and recommendations for Agent 2's decision
        DataStream<String> windowedStream = riskSignalsStream
                .keyBy(LoanRiskSignals::getApplicantId)
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(5)))
                .process(new AggregateLoanWindow())
                .name("loan-risk-window-aggregation");

        // --- STAGE 2: Generate final underwriting decision ---
        // The aggregated summary is evaluated by a senior-underwriter-persona LLM
        DataStream<LoanRiskReport> reportStream = agentsEnv
                .fromDataStream(windowedStream)
                .apply(new LoanRiskReportAgent())
                .toDataStream()
                .map(obj -> (LoanRiskReport) obj);

        // Kafka sink: final loan risk reports consumed by loan origination systems
        KafkaSink<LoanRiskReport> reportSink = KafkaSink.<LoanRiskReport>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("loan-risk-reports")
                                .setValueSerializationSchema(new LoanRiskReportSerializer())
                                .build())
                .build();

        reportStream
                .sinkTo(reportSink)
                .name("Kafka Sink: loan-risk-reports");

        agentsEnv.execute();
    }
}
