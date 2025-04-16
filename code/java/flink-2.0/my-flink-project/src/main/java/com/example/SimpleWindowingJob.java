package com.example;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.legacy.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

import java.time.Duration;
import java.util.Random;

public class SimpleWindowingJob {
    public static void main(String[] args) throws Exception {
        // Create Flink Environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Generate event stream
        DataStream<Tuple2<Integer, Integer>> stream = env.addSource(new SimpleEventSource());

        // Apply Tumbling Window of 5 seconds
        DataStream<Tuple2<Integer, Integer>> windowedStream = stream
                .keyBy(event -> event.f0) // Group by userId
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(5))) // 5-second window
                .sum(1); // Sum up counts per user

        // Print results
        windowedStream.print();

        // Execute Job
        env.execute("Simple Flink Windowing Job");
    }

    // Custom Source Function (Generates random events)
    public static class SimpleEventSource implements SourceFunction<Tuple2<Integer, Integer>> {
        private volatile boolean running = true;
        private Random random = new Random();

        @Override
        public void run(SourceContext<Tuple2<Integer, Integer>> ctx) throws Exception {
            for (int i = 0; i < 200 && running; i++) {
                Thread.sleep(100); // 10 events per second
                ctx.collect(new Tuple2<>(random.nextInt(5), 1)); // Random userId
            }
        }

        @Override
        public void cancel() {
            running = false;
        }
    }
}

/*
Build the JAR and paste in /tmp folder in the job manager and
docker exec -it my-flink-project-jobmanager-1 flink run -c com.example.SimpleWindowingJob /tmp/my-flink-project.jar

 */

/*
Flink hob
 */
