package com.example;

/**
 * Hello world!
 *
    */

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FileReaderJob {
    public static void main(String[] args) throws Exception {
        // Step 1: Create Flink execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Step 2: Read data from a file
        // DataStream<String> fileStream = env.readTextFile("input.txt");
        DataStream<String> fileStream = env.readTextFile("/tmp/input.txt");

        // Step 3: Process (simple logging)
        fileStream.map(new MapFunction<String, String>() {
            @Override
            public String map(String value) {
                System.out.println("Read Line: " + value);
                return value;
            }
        });

        // Step 4: Execute Flink Job
        env.execute("Simple File Reader Job");
    }
}

