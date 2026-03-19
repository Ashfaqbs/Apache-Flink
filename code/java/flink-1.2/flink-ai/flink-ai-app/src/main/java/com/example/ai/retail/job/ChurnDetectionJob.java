package com.example.ai.retail.job;

import com.example.ai.retail.agent.ChurnDetectionAgent;
import com.example.ai.retail.dto.ChurnAlert;
import com.example.ai.retail.dto.CustomerEvent;
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
 * Flink job that runs the ChurnDetectionAgent as a real-time streaming pipeline.
 *
 * Reads customer behavioral events from Kafka, applies a ReAct LLM agent to assess
 * churn risk, triggers automated retention actions via tools, and writes ChurnAlerts
 * to Kafka for downstream CRM and retention systems.
 *
 * Pipeline overview:
 *
 *   [Kafka: customer-events]
 *          |
 *          | (CustomerEvent JSON deserialized per record)
 *          v
 *   [ChurnDetectionAgent — Groq LLM via ReAct pattern]
 *          |   - Assesses churn risk: LOW / MEDIUM / HIGH / CRITICAL
 *          |   - Identifies churn signals (inactivity, support tickets, cart abandonment)
 *          |   - If HIGH or CRITICAL:
 *          |       -> calls triggerRetentionOffer(customerId, offerType)
 *          |       -> calls notifyCRMSystem(customerId, churnRisk, signals)
 *          |       -> optionally calls escalateToHumanAgent(customerId, reason)
 *          v
 *   [Kafka: churn-alerts]
 *          |
 *          v
 *   (Downstream: CRM system, retention dashboard, marketing automation)
 *
 * Why ReAct over Workflow here:
 *   - Churn patterns vary widely — not every customer needs every tool.
 *   - The LLM needs to reason about offer type selection, not just classify.
 *   - No aggregation window is needed — each event is evaluated independently.
 */
public class ChurnDetectionJob {

    public static void main(String[] args) throws Exception {
        // Fail fast if the API key is missing — prevents misleading runtime errors
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY environment variable is not set. " +
                    "Export it before submitting the job.");
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        AgentsExecutionEnvironment agentsEnv = AgentsExecutionEnvironment.getExecutionEnvironment(env);

        // Limit concurrent LLM calls to avoid Groq free-tier rate limits
        agentsEnv.getConfig().set(AgentExecutionOptions.NUM_ASYNC_THREADS, 2);

        // Register Groq via the OpenAI-compatible connection
        ResourceDescriptor groqConn = ResourceDescriptor.Builder
                .newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", groqApiKey)
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);

        // Kafka source: reads customer behavioral events as JSON
        KafkaSource<CustomerEvent> source = KafkaSource.<CustomerEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("customer-events")
                .setGroupId("churn-detection-group")
                .setDeserializer(
                        KafkaRecordDeserializationSchema.valueOnly(
                                new CustomerEventDeserializationSchema()))
                .build();

        DataStream<CustomerEvent> customerEventStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source: customer-events");

        // Apply the ReAct churn detection agent — each event is analyzed independently
        DataStream<ChurnAlert> churnAlertStream = agentsEnv
                .fromDataStream(customerEventStream)
                .apply(new ChurnDetectionAgent())
                .toDataStream()
                .map(obj -> (ChurnAlert) obj);

        // Kafka sink: writes ChurnAlert JSON to the churn-alerts topic
        KafkaSink<ChurnAlert> churnAlertSink = KafkaSink.<ChurnAlert>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("churn-alerts")
                                .setValueSerializationSchema(new ChurnAlertSerializer())
                                .build())
                .build();

        churnAlertStream
                .sinkTo(churnAlertSink)
                .name("Kafka Sink: churn-alerts");

        agentsEnv.execute();
    }
}
