package com.example.samplejob.kafkatokafka;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.time.Duration;


public class KafkaToKafkaFilterJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 1. Define Kafka Source
        KafkaSource<MyEvent2> source = KafkaSource.<MyEvent2>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("flink-group")
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new MyEventDeserializationSchemaold()))
                .build();

        // 2. Watermark Strategy
        WatermarkStrategy<MyEvent2> watermarkStrategy = WatermarkStrategy
                .<MyEvent2>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner((SerializableTimestampAssigner<MyEvent2>) (event, timestamp) ->
                        event.timestamp().toEpochMilli()
                );

        // 3. Read from Kafka
        DataStream<MyEvent2> stream = env.fromSource(source, watermarkStrategy, "Kafka Source");

        // 4. Processing: Filter use case (e.g., only keep events with ID > 100)
        DataStream<MyEvent2> filteredStream = stream.filter(event -> event.id() > 100)
                .name("Filter ID > 100");

        // 5. Define Kafka Sink
        KafkaSink<MyEvent2> sink = KafkaSink.<MyEvent2>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new MyEventSerializerold())
                        .build())
                .build();

        // 6. Send it to Kafka
        filteredStream.sinkTo(sink).name("Kafka Sink");

        env.execute("Kafka to Kafka Filter Job");
    }
}