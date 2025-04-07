package com.example;

import com.example.dto.UserPlain;
import com.example.kafka.KafkaUserDeserializationSchema;
import com.example.sink.PostgresSink;
import com.example.state.UserEventCountFunction;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
//import org.apache.flink.api.common.serialization.SimpleStringSchema; when sending as string
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

import java.time.Duration;

public class FlinkKafkaToDB {
/*
Flink Kafka to DB Job
with Checkpointing, Stateful Processing, Tumbling Window, Transformations and Filtering
 */
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        // Enable Checkpointing (Every 5 Seconds)
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().enableUnalignedCheckpoints();
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);


       // Kafka Source reading as string
//        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
//                .setBootstrapServers("kafka:9093")
//                .setTopics("my-topic")
//                .setGroupId("flink-group")
//                .setStartingOffsets(OffsetsInitializer.earliest())
//                .setValueOnlyDeserializer(new SimpleStringSchema())
//                .build();

//        DataStream<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");



// Custom Deserializer
        KafkaSource<UserPlain> kafkaSource = KafkaSource.<UserPlain>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("my-topic")
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest()) // ✅ Start from the beginning
                .setValueOnlyDeserializer(new KafkaUserDeserializationSchema()) // Custom deserializer
                .build();

        DataStream<UserPlain> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");



        // Read from Kafka where user as string
        // Parse JSON not required as we are using custom deserializer
//        DataStream<UserPlain> userStream = kafkaStream.map(json -> {
//            ObjectMapper mapper = new ObjectMapper();
//            UserPlain user = mapper.readValue(json, UserPlain.class);
//
//           return user;
//        });





// ​In a 30-second tumbling window, Apache Flink groups all incoming records that arrive within each 30-second interval into a single batch.
        DataStream<Long> userCounts = kafkaStream
                .windowAll(TumblingProcessingTimeWindows.of(Duration.ofSeconds(30)))
                .aggregate(new AggregateFunction<UserPlain, Long, Long>() {
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    @Override
                    public Long add(UserPlain value, Long accumulator) {
                        return accumulator + 1;
                    }

                    @Override
                    public Long getResult(Long accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Long merge(Long a, Long b) {
                        return a + b;
                    }
                });


        System.out.println("Tumbling Window, Records processed for every 30 seconds "+ userCounts.print());



        // ✅ Stateful Processing (Per User)
        DataStream<String> statefulCounts = kafkaStream
                .keyBy(UserPlain::getName) // Key by user name to test hardcode the name in app.py
                .flatMap(new UserEventCountFunction()); // Custom Stateful Processing

        System.out.println("Stateful Processing "+ statefulCounts.print()); ;



//        ** Transformations


//        Individual Transformations

//        DataStream<User> processedStream = userStream
//                .filter(user -> user.getId() > 100)
//                .map(user -> {
//                    user.setName(user.getName().toUpperCase());
//                    return user;
//                });
//
//
//        DataStream<User> transformedStream = filteredStream.map(user -> {
//            user.setName(user.getName().toUpperCase());
//            return user;
//        });



        //        Chain Transformations
        DataStream<UserPlain> processedStream = kafkaStream
                .filter(user -> user.getId() > 100)
                .map(user -> {
                    user.setName(user.getName().toUpperCase());
                    System.out.println(user.toString() + " " + user.id + " " + user.name);
                    return user;
                });



        processedStream.addSink(new PostgresSink());


        env.execute("Flink Kafka to PostgreSQL DB with states and window");
    }


}
