package com.example.jobs;


import com.example.config.ReviewDeserializer;
import com.example.config.ReviewSentimentSerializer;
import com.example.dto.ReviewSentimentDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KafkaToKafkaJob {

    private static final String API_URL = "http://host.docker.internal:8000/predict_sentiment/";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("flink-review-group")
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new ReviewDeserializer()))
                .build();

        DataStream<String> inputStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Input");

        DataStream<ReviewSentimentDTO> outputStream = inputStream.map(review -> {
            HttpClient client = HttpClient.newHttpClient();

            String requestBody = "{\"review_text\": \"" + review.replace("\"", "\\\"") + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String sentiment = extractSentiment(response.body());
            return new ReviewSentimentDTO(review, sentiment);
        });

        KafkaSink<ReviewSentimentDTO> sink = KafkaSink.<ReviewSentimentDTO>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("output-topic")
                        .setValueSerializationSchema(new ReviewSentimentSerializer())
                        .build())
                .build();

        outputStream.sinkTo(sink);

        env.execute("Kafka to Kafka ML Prediction Job");
    }

    private static String extractSentiment(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            return root.get("sentiment").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse API response: " + responseBody, e);
        }
    }

}
