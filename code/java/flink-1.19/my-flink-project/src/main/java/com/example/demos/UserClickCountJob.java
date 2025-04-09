package com.example.demos;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class UserClickCountJob {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<UserEvent> source = KafkaSource.<UserEvent>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("flink-group")
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new UserEventDeserializer()))
                .build();

        DataStream<UserEvent> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        DataStream<String> result = stream
                .keyBy(UserEvent::getUserId)
                .process(new StatefulClickCounter());

        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .build();

        result.sinkTo(sink);
        env.execute("User Click Count with State");
    }

}
