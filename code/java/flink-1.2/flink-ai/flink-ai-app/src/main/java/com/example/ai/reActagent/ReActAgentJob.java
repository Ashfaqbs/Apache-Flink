package com.example.ai.reActagent;

import org.apache.flink.agents.api.AgentsExecutionEnvironment;
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

public class ReActAgentJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        AgentsExecutionEnvironment agentsEnv = AgentsExecutionEnvironment.getExecutionEnvironment(env);

        // 1. Register Groq (Using the correct 'baseUrl' parameter)
        ResourceDescriptor groqConn = ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", "API_KEY") // <--- PASTE YOUR API KEY
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);

        // 2. Kafka Source (Using the new Entity and a fresh Consumer Group)
        KafkaSource<ReActEvent> source = KafkaSource.<ReActEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("react-agent-group-v1") // Fresh group ID!
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new ReActEventDeserializationSchema()))
                .build();

        DataStream<ReActEvent> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // 3. Apply the ReAct Agent
        DataStream<ReActEvent> agentStream = agentsEnv
                .fromDataStream(stream)
                .apply(new ReActCategorizerAgent())
                .toDataStream()
                .map(obj -> (ReActEvent) obj); // Cast back to ReActEvent

        // 4. Kafka Sink
        KafkaSink<ReActEvent> sink = KafkaSink.<ReActEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new ReActEventSerializer())
                        .build())
                .build();

        agentStream.sinkTo(sink).name("ReAct Agent Sink");

        agentsEnv.execute();
    }
}