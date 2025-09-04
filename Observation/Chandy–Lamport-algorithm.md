# 1. What is Checkpointing in Flink?

Checkpointing is **Flink’s mechanism to periodically persist state** so that the job can recover from failures without losing correctness.

Think of it as **periodic snapshots** of:

* **Operator state** (what each task/transform has in memory, e.g., aggregations, windows, buffers).
* **Input source positions** (like Kafka offsets).

These snapshots are saved to **durable storage** (in our example, S3). On recovery, Flink reloads them and resumes from where it left off.

---

# 2. Flink Configurations for Checkpointing

In Flink’s `flink-conf.yaml` or programmatic config, you’ll typically see:

```yaml
state.backend: rocksdb                # or filesystem, hashmap
state.checkpoints.dir: s3://my-bucket/flink-checkpoints
execution.checkpointing.interval: 5000ms   # every 5 seconds
execution.checkpointing.mode: EXACTLY_ONCE
execution.checkpointing.timeout: 10min
execution.checkpointing.max-concurrent-checkpoints: 1
execution.checkpointing.unaligned: true    # for backpressure heavy pipelines
```

**What they do:**

* `state.backend` → where operator state lives in memory or on disk (e.g., RocksDB).
* `state.checkpoints.dir` → durable storage for checkpoints.
* `interval` → frequency (e.g., every 5 sec).
* `mode` → Exactly-once vs At-least-once.
* `timeout` → how long a checkpoint can take before failing.
* `max-concurrent-checkpoints` → parallel vs sequential checkpoints.
* `unaligned` → helps when there is backpressure.

---

# 3. Example Pipeline: Kafka → Map → Filter → Sink (S3)

### Code Sketch

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

env.enableCheckpointing(5000); // every 5 sec

// Source: Kafka
FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
    "input-topic",
    new SimpleStringSchema(),
    properties
);
DataStream<String> stream = env.addSource(kafkaSource);

// Operator 1: Map
DataStream<String> mapped = stream.map(value -> value.toUpperCase());

// Operator 2: Filter
DataStream<String> filtered = mapped.filter(value -> value.startsWith("A"));

// Sink: S3
StreamingFileSink<String> sink = StreamingFileSink
    .forRowFormat(new Path("s3://my-bucket/output"), new SimpleStringEncoder<String>("UTF-8"))
    .build();

filtered.addSink(sink);

env.execute("Kafka-to-S3 Example");
```

---

### Operators in This Context

* **Source Operator (Kafka)**: Reads messages, keeps track of **Kafka offsets**.
* **Map Operator**: Transforms records (stateless, but checkpointing still ensures consistency).
* **Filter Operator**: Drops some records (also stateless here).
* **Sink Operator (S3)**: Buffers and writes files in S3, maintains **in-progress files** as part of state.

---

# 4. How Checkpointing Works in This Example

* Every **5 seconds**, Flink’s JobManager triggers a **checkpoint**.
* A **checkpoint barrier** is inserted into each Kafka partition stream at the source.
* The barrier flows **through the pipeline**:

### Step-by-Step:

1. **Source (Kafka)**:

   * Saves the current **Kafka offsets** in the checkpoint.
   * Emits a **barrier** into the stream.

2. **Map Operator**:

   * Waits until it has received the barrier.
   * Takes its own **state snapshot** (none here since it’s stateless).
   * Forwards the barrier downstream.

3. **Filter Operator**:

   * Same as map: takes snapshot (stateless) + forwards barrier.

4. **Sink (S3)**:

   * Receives barrier.
   * Persists metadata about **which files are in-progress**.
   * Ensures that on recovery, it either **commits completed files** or **replays buffered ones** consistently.
   * Writes barrier forward (if needed).

5. Once all operators acknowledge completion, the checkpoint is **finalized** in S3 at:
   `s3://my-bucket/flink-checkpoints/<checkpoint-id>/`

So at `t = 5s, 10s, 15s...` → snapshots of Kafka offsets + sink state are stored.

---

# 5. Chandy–Lamport Snapshot Algorithm

### The Algorithm in General

The Chandy–Lamport algorithm is designed to capture a **consistent global state** of a distributed system **without stopping the system**.

**Steps:**

1. A coordinator injects a **marker** (checkpoint barrier) into all channels.
2. Each process (operator) on receiving the first marker:

   * Records its local state.
   * Forwards the marker downstream.
3. For other incoming channels, until a marker arrives, it records **all incoming messages** (these are "in-flight" messages).
4. When markers have arrived from **all inputs**, the operator’s snapshot is complete.

---

### Analogy

Imagine multiple **security cameras** (operators) monitoring a city (the dataflow). The mayor (JobManager) sends a **red flare** (barrier) into the sky.

* Each camera, when it first sees the flare, **takes a photo of what it sees (local state)**.
* For streets it hasn’t yet received the flare, it keeps recording cars passing until the flare reaches that street.
* Once all streets have shown the flare, the camera has a **complete snapshot**.
* All photos together form a **consistent city snapshot at that instant**.

---

# 6. Correlation of Our Kafka → S3 Example to Chandy–Lamport

* **Marker = Checkpoint Barrier** (inserted at Kafka source every 5s).
* **Local state = operator state** (Kafka offset, sink buffers, etc.).
* **Messages in transit = unprocessed records between operators when barrier flows.**
* **Consistency guarantee**:

  * Suppose Kafka emitted 100 records, barrier comes after record #100.
  * All operators checkpoint after processing up to record #100.
  * On recovery: Flink restores Kafka offset = 101 and state = “post-100”.
  * No record is lost or processed twice.

So, Flink essentially applies **Chandy–Lamport** on a **dataflow graph with barriers instead of markers**.

---

# ✅ Final Summary

* **Checkpointing in Flink** = periodic consistent snapshots of state and source positions (stored in S3 here).
* **In Kafka → Map → Filter → S3 example**: every 5s, Kafka offsets + sink buffers are checkpointed; stateless operators just pass barriers along.
* **Chandy–Lamport algorithm** is the theoretical backbone: barrier (marker) propagation ensures that snapshots across distributed operators form a **consistent global state**.
* Recovery guarantees exactly-once semantics: process resumes as if no failure happened.

---
