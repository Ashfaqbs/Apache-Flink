package com.example.jobs;

import com.example.config.VisaUserDeserializer;
import com.example.dto.VisaUser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

public class KafkaToDbeg2 {

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Create Kafka Source
        KafkaSource<VisaUser> source = KafkaSource.<VisaUser>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("input-topic")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new VisaUserDeserializer()))
                .build();


        // 2. Create DataStream from the Source
        DataStream<VisaUser> rawUserStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "VisaUser Kafka Source"
        );

// 3. Filter out "Tourist" visa users
        DataStream<VisaUser> filteredUsers = rawUserStream
                .filter(user -> !"Tourist".equalsIgnoreCase(user.getVisaType()))
                .name("Filter Non-Tourists");

// 4. Transform user data (uppercase names)
        DataStream<VisaUser> transformedUsers = filteredUsers
                .map(user -> {
                    user.setName(user.getName().toUpperCase());
                    return user;
                })
                .name("Uppercase Name Transformation");


//  🔔 Event reaction: log if user id == 0 another eg is we can use log if user age > 40
        transformedUsers
                .filter(user -> user.getId() ==0)
                .map(user -> {
                    System.out.println("👴 Invalid User : " + user.getName() + ", id: " + user.getId());
                    return user;
                })
                .name("ID-based Logging (ID == 40)");


        // 5. Write to PostgreSQL
        transformedUsers.addSink(JdbcSink.sink(
                "INSERT INTO public.visa_users (name, country, visa_type) VALUES (?, ?, ?)",
                (ps, user) -> {
                    System.out.println("VisaUser User: " + user);
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getCountry());
                    ps.setString(3, user.getVisaType());
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(3)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://postgres:5432/mainschema")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("postgres")
                        .withPassword("admin")
                        .build()
        )).name("PostgreSQL Sink");


        try {
            env.execute("Kafka to PostgreSQL Job");
        } catch (Exception e) {
            System.err.println("Fatal error in Flink Job: " + e.getMessage());
            e.printStackTrace();        }

    }
}
