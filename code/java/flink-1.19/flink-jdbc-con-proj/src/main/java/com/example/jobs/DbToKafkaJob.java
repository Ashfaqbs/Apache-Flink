package com.example.jobs;

import com.example.config.VisaUserSerializer;
import com.example.dto.VisaUser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.jdbc.source.JdbcSource;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DbToKafkaJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        JdbcSource<VisaUser> jdbcSource = JdbcSource.<VisaUser>builder()
                .setDBUrl("jdbc:postgresql://postgres:5432/mainschema")
                .setUsername("postgres")
                .setPassword("admin")
                .setDriverName("org.postgresql.Driver")
                .setSql("SELECT id, name, country, visa_type FROM public.visa_users")
                .setResultExtractor(resultSet -> {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String country = resultSet.getString("country");
                    String visaType = resultSet.getString("visa_type");
                    VisaUser visaUser = new VisaUser(id, name, country, visaType);
                    System.out.println("From DB VisaUser: " + visaUser);
                    return visaUser;
                })
                .setTypeInformation(TypeInformation.of(VisaUser.class))
                .build();




        DataStreamSource<VisaUser> visaUserStream = env.fromSource(
                jdbcSource,
                WatermarkStrategy.noWatermarks(),
                "Postgres VisaUser Source"
        );



        KafkaSink<VisaUser> sink = KafkaSink.<VisaUser>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic("my-topic")
                 .setValueSerializationSchema(new VisaUserSerializer())
                .build())
                .build();


            // Send it to Kafka
            visaUserStream.sinkTo(sink).name("Kafka Sink");

        env.execute("Read from DB and Write to Kafka");
    }
}
