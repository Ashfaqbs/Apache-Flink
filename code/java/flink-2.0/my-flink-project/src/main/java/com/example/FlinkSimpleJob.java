package com.example;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkSimpleJob {
    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Generate a sequence of numbers from 1 to 200
        DataStream<Long> numbers = env.generateSequence(1, 200);

        // Map each number to a string with a prefix
        DataStream<String> messages = numbers.map(new MapFunction<Long, String>() {
            @Override
            public String map(Long value) {
                return "Generated number: " + value;
            }
        });

        // Print each message to the console
        messages.print();

        // Execute the Flink job
        env.execute("Simple Flink v2.9 J17 Job");
    }

}
/*
Exectuion and output found in output / s0 folder
 */