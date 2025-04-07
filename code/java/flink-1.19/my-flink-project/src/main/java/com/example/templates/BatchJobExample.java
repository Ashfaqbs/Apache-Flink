package com.example.templates;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class BatchJobExample {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);  // Explicitly set batch mode

        DataStream<String> batchData = env.readTextFile("file:///path/to/big.txt");  // Static file (finite)

        batchData.print();  // Just printing for now
        try {
            env.execute("Batch Job Example");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
