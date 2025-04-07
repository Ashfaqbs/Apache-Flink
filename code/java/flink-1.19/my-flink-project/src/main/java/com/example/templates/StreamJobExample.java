package com.example.templates;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamJobExample {
    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Default mode is STREAMING, so we don’t need to set it explicitly

        DataStream<String> streamData = env.fromSource(
                KafkaSource.<String>builder()
                        .setBootstrapServers("kafka:9093")
                        .setTopics("my-topic")
                        .setGroupId("flink-group")
                        .setStartingOffsets(OffsetsInitializer.latest())
                        .build(),
                WatermarkStrategy.noWatermarks(),
                "Kafka Source"
        );

        streamData.print();
        try {
            env.execute("Streaming Job Example");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
