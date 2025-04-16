### Understanding Flink's Streaming vs. Batch Behavior in a JDBC-to-Kafka Job

Apache Flink is commonly referred to as a stream processing engine. However, it also supports bounded (batch) processing under the same APIs, making it a unified platform for both streaming and batch workloads.

#### Background Scenario

In a typical Flink pipeline, a job was created that reads data from a PostgreSQL database and writes it to a Kafka topic. The source is a bounded JDBC table, and the destination is an unbounded Kafka topic. The Flink job was written using the DataStream API, which is typically associated with streaming use cases.

#### Question

If Flink is designed for unbounded streaming data, how does it handle a job that reads from a bounded source such as a relational database? Is it still a streaming job?

#### Explanation

Even though Flink's DataStream API is used, the job behaves like a batch pipeline because the JDBC source is inherently bounded. Flink internally detects that the source is bounded and optimizes execution accordingly.

The `JdbcSource` class, which is part of the `flink-connector-jdbc` module, implements the method:
```java
public Boundedness getBoundedness() {
    return Boundedness.BOUNDED;
}
```
This method declares the source as bounded, which instructs Flink that the source will produce a finite dataset. While this method is not heavily documented in the official user-facing API documentation, it is part of the source implementation and plays a key role in how Flink determines the runtime behavior of the job.

Therefore, while the syntax and APIs used in the job resemble a streaming pipeline, the job execution resembles batch behavior. The job processes the finite input and terminates upon completion, which is characteristic of a batch job.

#### Using RuntimeExecutionMode

Flink allows explicit control over the execution mode via:
```java
env.setRuntimeMode(RuntimeExecutionMode.BATCH);
```

This configuration forces the job to be treated as a batch job regardless of other parameters. It results in the following changes:

- Watermarking and event-time processing are disabled.
- Timer services are not initialized.
- Operators may be fused and optimized for throughput.
- No need for checkpointing.

This setting can be beneficial when the pipeline is known to be bounded, such as when reading from a database or a file. It ensures that all operators and sinks are executed with batch-optimized semantics.

#### Example: JDBC to Kafka Flink Job

```java
public class DbToKafkaJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH); // Explicitly set to batch mode

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
                    return new VisaUser(id, name, country, visaType);
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

        visaUserStream.sinkTo(sink).name("Kafka Sink");

        env.execute("Read from DB and Write to Kafka");
    }
}
```

This example illustrates the use of a bounded source (`JdbcSource`) and a streaming sink (`KafkaSink`) within a job explicitly configured for batch execution.

#### Comparison Table

| Feature                         | Streaming Mode (Default) | Batch Mode (Explicit)       |
|--------------------------------|---------------------------|------------------------------|
| Source Type                    | Bounded or Unbounded      | Bounded only                 |
| Watermark Support              | Enabled                   | Disabled                     |
| Timers and Windows             | Fully supported           | Limited                      |
| Checkpointing                  | Optional                  | Not used                     |
| Operator Behavior              | Incremental               | Bulk, optimized              |
| JDBC Source Handling           | Auto-detected as bounded  | Treated as bounded           |
| Sink Completion                | Waits for termination     | Flushes immediately          |

When to Use RuntimeExecutionMode.BATCH
Use it when:

we know our source is 100% bounded (JDBC, file, collection)

we're building data pipelines, not infinite event processors

we want to speed up batch-style flows

```commandline
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setRuntimeMode(RuntimeExecutionMode.BATCH); //  Adding this line  now it’s 100% optimized for batch-style execution.
```

#### Conclusion

Using the DataStream API with a bounded source like JDBC results in behavior that mimics batch processing. While the default execution mode is `STREAMING`, explicitly setting `RuntimeExecutionMode.BATCH` helps Flink optimize the execution further. This approach is especially suitable for one-time ETL jobs, data migration tasks, or bootstrapping Kafka topics from databases.

Although the method `getBoundedness()` is not prominently featured in the official documentation, it is an important internal mechanism through which Flink distinguishes bounded and unbounded sources, enabling intelligent execution plan adjustments even when using the streaming API.




### Flink Runtime Behavior Without vs. With Batch Mode

In a Flink job where `env.setRuntimeMode(RuntimeExecutionMode.BATCH)` is not explicitly set, Flink defaults to `STREAMING` mode. This section explores what happens in that default state and how behavior changes when batch mode is explicitly enabled.

#### Current Behavior (Default Streaming Mode)

When `env.setRuntimeMode(...)` is not called, Flink chooses `RuntimeExecutionMode.STREAMING` by default. In this mode:

- **Boundedness Detection**: Flink automatically detects that the JDBC source is bounded (`Boundedness.BOUNDED`).
- **Execution Mode**: The execution is still treated as a streaming job.
- **Watermarks**: Even though the job doesn't use event time or timers, watermark logic remains initialized.
- **Operators**: All operators function incrementally, as if processing a continuous stream.
- **Job Completion**: The job completes after consuming all data from the bounded source.

This configuration works because Flink is smart enough to recognize bounded sources, even under the streaming runtime, and optimizes execution accordingly.

#### Modified Behavior (Explicit Batch Mode)

When `env.setRuntimeMode(RuntimeExecutionMode.BATCH)` is added to the job, it signals Flink to apply batch-specific execution strategies across the entire pipeline:

- **Watermarks**: Watermark generation and event-time tracking are skipped entirely.
- **Timer Services**: Not initialized.
- **Operator Execution**: Uses bulk/batch-style execution. Operators are fused and optimized.
- **Checkpointing**: Disabled.
- **Sink Behavior**: Kafka sink flushes all data and completes without waiting for unbounded input.

This leads to better performance, especially for finite data sets where streaming semantics and latency guarantees are unnecessary.

#### Summary

| Characteristic                 | Default (Streaming)         | With Batch Mode                 |
|-------------------------------|------------------------------|----------------------------------|
| Execution Mode                | STREAMING                    | BATCH                           |
| Watermarks                    | Initialized (unused here)    | Disabled                        |
| Timers                        | Active                       | Inactive                        |
| Operator Optimization         | Incremental                  | Bulk                            |
| Checkpointing                 | Possible                     | Not used                        |
| Performance for Bounded Input | Good                         | Better (optimized for batch)    |
| Job Completion                | After source ends            | Same (but more efficient)       |

#### Conclusion

Not setting the runtime mode still allows Flink to complete a job from a bounded JDBC source correctly, but setting `RuntimeExecutionMode.BATCH` leads to more efficient execution. For jobs that only deal with bounded data, explicitly using batch mode is a good practice to maximize performance and clarity.

