package com.example.ai.banking.job;

import com.example.ai.banking.agent.FraudDetectionAgent;
import com.example.ai.banking.dto.FraudAlert;
import com.example.ai.banking.dto.Transaction;
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

/**
 * Flink job that runs the FraudDetectionAgent as a real-time streaming pipeline.
 *
 * Reads bank transactions from Kafka, applies a ReAct LLM agent to assess fraud risk,
 * and writes FraudAlert results back to Kafka for downstream fraud operations teams.
 *
 * Pipeline overview:
 *
 *   [Kafka: bank-transactions]
 *          |
 *          | (Transaction JSON deserialized by TransactionDeserializationSchema)
 *          v
 *   [FraudDetectionAgent — Groq LLM via ReAct pattern]
 *          |   - Assesses risk: LOW / MEDIUM / HIGH / CRITICAL
 *          |   - If HIGH or CRITICAL:
 *          |       -> calls blockCard(accountId, reason) tool
 *          |       -> calls alertFraudTeam(details, severity) tool
 *          |
 *          v
 *   [Kafka: fraud-alerts]
 *          |
 *          v
 *   (Downstream: fraud ops dashboard, card management system, compliance reporting)
 *
 * Key design decisions:
 *   - ReAct pattern chosen over workflow because fraud detection needs conditional
 *     tool use based on LLM reasoning, not a fixed multi-agent pipeline.
 *   - NUM_ASYNC_THREADS=2 limits Groq API concurrency to stay within rate limits.
 *   - GROQ_API_KEY must be set as an environment variable — never hardcoded.
 */
public class FraudDetectionJob {

    public static void main(String[] args) throws Exception {
        // Fail fast if the API key is missing — prevents silent failures at runtime
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY environment variable is not set. " +
                    "Export it before submitting the job.");
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        AgentsExecutionEnvironment agentsEnv = AgentsExecutionEnvironment.getExecutionEnvironment(env);

        // Limit concurrent LLM calls to avoid hitting Groq's free-tier rate limits
        agentsEnv.getConfig().set(AgentExecutionOptions.NUM_ASYNC_THREADS, 2);

        // Register Groq using the OpenAI-compatible connection descriptor
        ResourceDescriptor groqConn = ResourceDescriptor.Builder
                .newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", groqApiKey)
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);

        // Kafka source: reads transactions as JSON from the bank-transactions topic
        KafkaSource<Transaction> source = KafkaSource.<Transaction>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("bank-transactions")
                .setGroupId("fraud-detection-group")
                .setDeserializer(
                        KafkaRecordDeserializationSchema.valueOnly(
                                new TransactionDeserializationSchema()))
                .build();

        DataStream<Transaction> transactionStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source: bank-transactions");

        // Apply the ReAct fraud detection agent — each transaction is analyzed independently
        DataStream<FraudAlert> alertStream = agentsEnv
                .fromDataStream(transactionStream)
                .apply(new FraudDetectionAgent())
                .toDataStream()
                .map(obj -> (FraudAlert) obj);

        // Kafka sink: writes FraudAlert JSON to the fraud-alerts topic
        KafkaSink<FraudAlert> fraudAlertSink = KafkaSink.<FraudAlert>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("fraud-alerts")
                                .setValueSerializationSchema(new FraudAlertSerializer())
                                .build())
                .build();

        alertStream
                .sinkTo(fraudAlertSink)
                .name("Kafka Sink: fraud-alerts");

        agentsEnv.execute();
    }
}
