package com.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SimpleKafkaToKafkaJob {
/*
Flink Job to consume from Kafka Topic "input-topic" and produce to Kafka Topic "output-topic" simple strings
 */
    public static void main(String[] args) throws Exception {
        // Setup Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define Kafka Source (Consuming from "input-topic")
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("flink-consumer-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Define Kafka Sink (Producing to "output-topic")
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .build();

        // Create DataStream from Kafka Source
        DataStream<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // (Optional) Process Data: Example - Convert to Uppercase
        DataStream<String> processedStream = kafkaStream.map(String::toUpperCase);

        // Send processed data to Kafka Sink
        processedStream.sinkTo(kafkaSink);

        // Execute Flink Job
        env.execute("Simple Kafka to Kafka Flink Job");
    }
}
