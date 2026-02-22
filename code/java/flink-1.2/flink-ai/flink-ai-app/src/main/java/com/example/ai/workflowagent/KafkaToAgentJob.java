package com.example.ai.workflowagent;

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

public class KafkaToAgentJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Wrap standard Flink env in the new Agents env
        AgentsExecutionEnvironment agentsEnv = AgentsExecutionEnvironment.getExecutionEnvironment(env);


//        OLLAMA but timesout
//        // 1. Register Ollama Connection (Using the official Flink Agents constant)
//        ResourceDescriptor ollamaConn = ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OLLAMA_CONNECTION)
//                .addInitialArgument("endpoint", "http://host.docker.internal:11434")
//                .build();
//        agentsEnv.addResource("ollamaConnection", ResourceType.CHAT_MODEL_CONNECTION, ollamaConn);



//        OPEN AI VIA GROQ

        // 1. Register Groq via the OpenAI Connection Standard
        ResourceDescriptor groqConn = ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_CONNECTION)
                .addInitialArgument("api_base_url", "https://api.groq.com/openai/v1")
                .addInitialArgument("api_key", System.getenv("GROQ_API_KEY")) // <-- Paste your groq api key
                .build();

        agentsEnv.addResource("groqConnection", ResourceType.CHAT_MODEL_CONNECTION, groqConn);


        // 2. Define Kafka Source (localhost:9093 inside Docker)
        KafkaSource<MyEvent> source = KafkaSource.<MyEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("ai-agent-group-v2")
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new MyEventDeserializationSchema()))
                .build();

        DataStream<MyEvent> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // 3. THE MAGIC: Apply the Flink Agent to the Stream
        DataStream<MyEvent> agentStream = agentsEnv
                .fromDataStream(stream)
                .apply(new EventCategorizerAgent())
                .toDataStream()
                .map(obj -> (MyEvent) obj); // Cast Flink Agent OutputEvent back to MyEvent

        // 4. Define Kafka Sink
        KafkaSink<MyEvent> sink = KafkaSink.<MyEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new MyEventSerializer())
                        .build())
                .build();

        // 5. Send enriched data to Kafka
        agentStream.sinkTo(sink).name("Kafka AI Sink");

        agentsEnv.execute();
    }
}