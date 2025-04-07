package com.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;

public class GitHubFileToKafka {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // GitHub raw file URL
        String fileUrl = "https://raw.githubusercontent.com/Ashfaqbs/Apache-Flink/refs/heads/main/input/big.txt";

        // Fetch the file content and create a data stream
        DataStream<String> fileStream = env.fromElements(fetchFileContent(fileUrl))
                .flatMap((String content, Collector<String> out) -> {
                    Stream.of(content.split("\n")).forEach(out::collect);
                }).returns(String.class)
                .filter(line -> !line.trim().isEmpty()) // Filter out empty lines
                .name("GitHub File Source");

        // Kafka Sink Configuration
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("my-topic")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .build();


        fileStream.print();

        // Send each line to Kafka topic
        fileStream.sinkTo(kafkaSink).name("Kafka Sink");

        // Execute Flink Job
        env.execute("GitHub File to Kafka Streaming Job");
    }

    // Function to fetch file content from the GitHub URL
    private static String fetchFileContent(String fileUrl) throws Exception {
        StringBuilder content = new StringBuilder();
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }
}
