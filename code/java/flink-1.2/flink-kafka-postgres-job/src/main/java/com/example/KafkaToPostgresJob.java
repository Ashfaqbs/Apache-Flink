package com.example;


import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

public class KafkaToPostgresJob {
    public static void main(String[] args) throws Exception {
        // Set up Flink streaming environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka properties
        Properties properties = new Properties();
//      properties.setProperty("bootstrap.servers", "localhost:9092");
        // properties.setProperty("bootstrap.servers", "kafka:9092"); // Docker service name
        properties.setProperty("bootstrap.servers", "kafka:9093");
        properties.setProperty("group.id", "flink-consumer");

        // Create Kafka Consumer
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                "my-topic",
                new SimpleStringSchema(),
                properties
        );

        // Add Kafka source
        DataStream<String> stream = env.addSource(kafkaConsumer);

        // Transform: Convert JSON -> Uppercase Name
        DataStream<User> transformedStream = stream.map(new MapFunction<String, User>() {
            @Override
            public User map(String value) throws Exception {
                // Convert JSON string to Java object
                String json = value.replaceAll("[{}\"]", ""); // Simple parsing (use Gson/Jackson for real use)
                String[] parts = json.split(",");
                int id = Integer.parseInt(parts[0].split(":")[1].trim());
                String name = parts[1].split(":")[1].trim().toUpperCase(); // Transform: Uppercase Name

                return new User(id, name);
            }
        });

        // Insert into PostgreSQL
        // Use PostgresSink instead of a lambda function
        transformedStream.addSink(new PostgresSink());



//        transformedStream.addSink((User user) -> {  // Specify User type
//            try (Connection conn = DriverManager.getConnection(
//                    "jdbc:postgresql://localhost:9991/mainschema", "postgres", "admin");
//                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO flusers (id, name) VALUES (?, ?)")) {
//
//                stmt.setInt(1, user.getId());
//                stmt.setString(2, user.getName());
//                stmt.executeUpdate();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });





        // Execute Flink Job
        env.execute("Kafka to PostgreSQL Flink Job");
    }

    public static class User {
        private int id;
        private String name;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }


}