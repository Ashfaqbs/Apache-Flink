package com.example;

import com.example.dto.MyEvent;
import com.example.kafka.MyEventDeserializationSchema;
import com.example.kafka.MyEventSerializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import java.time.Duration;

public class KafkaToKafkaJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define Kafka Source
        KafkaSource<MyEvent> source = KafkaSource.<MyEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("flink-group")
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new MyEventDeserializationSchema()))
                .build();

        // Watermark Strategy
        WatermarkStrategy<MyEvent> watermarkStrategy = WatermarkStrategy
                .<MyEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner((SerializableTimestampAssigner<MyEvent>) (event, timestamp) ->
                        event.getTimestamp().toEpochMilli()
                );

        // Read from Kafka
        DataStream<MyEvent> stream = env.fromSource(source, watermarkStrategy, "Kafka Source");

        // Processing: Convert name to uppercase
        DataStream<MyEvent> processedStream = stream.map(event ->
                new MyEvent(event.getId(), event.getName().toUpperCase(), event.getTimestamp()));

        // Define Kafka Sink
        KafkaSink<MyEvent> sink = KafkaSink.<MyEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new MyEventSerializer())
                        .build())
                .build();


        // Send it to Kafka
        processedStream.sinkTo(sink);

        env.execute("Kafka to Kafka Transformation");
    }
}
