package com.example;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);  // Explicitly set batch mode

        DataStream<String> batchData = env.readTextFile("file:///path/to/big.txt");  // Static file (finite)

        batchData.print();  // Just printing for now
        env.execute("Batch Job Example");



    }


}
