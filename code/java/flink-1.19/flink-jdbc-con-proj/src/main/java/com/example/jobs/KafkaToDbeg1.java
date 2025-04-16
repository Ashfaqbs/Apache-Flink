package com.example.jobs;
import com.example.dto.FlUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KafkaToDbeg1 {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Create Kafka Source
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("my-topic")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Read from Kafka
        DataStream<String> kafkaStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source"
        );

        // Map JSON to FlUser object
        DataStream<FlUser> userStream = kafkaStream.map(json -> {
            ObjectMapper mapper = new ObjectMapper();
            FlUser user = mapper.readValue(json, FlUser.class);
            System.out.println("Kafka User: " + user);
            return user;
        }).returns(FlUser.class);

        // Sink to PostgreSQL
        userStream.addSink(JdbcSink.sink(
                "INSERT INTO public.flusers (name) VALUES (?)",
//                (statement, user) -> {
//                    statement.setString(1, user.getName()
//
//
//                    );
//                }
                (statement, user) -> {
                    try {
                        System.out.println("Inserting user: " + user);
                        statement.setString(1, user.getName());
                    } catch (Exception e) {
                        System.err.println("DB Sink error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
               ,
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(3)
                        .build()
                ,
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://postgres:5432/mainschema")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("postgres")
                        .withPassword("admin")
                        .build()
        )).name("PostgreSQL Sink");

        // Execute the Flink job
        try {
            env.execute("Kafka to Postgres JDBC Connector Example");
        } catch (Exception e) {
            System.err.println("Failed to execute the job: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

/*
PS C:\tmp\afjdbccon\flink-jdbc-con-proj\docker\jars> docker cp .\db.jar docker-jobmanager-1:/tmp/db.jar
Successfully copied 82.2MB to docker-jobmanager-1:/tmp/db.jar
PS C:\tmp\afjdbccon\flink-jdbc-con-proj\docker\jars> docker exec -it docker-jobmanager-1 /opt/flink/bin/flink run -c com.example.jobs.KafkaToDbeg1   /tmp/db.jar

 */