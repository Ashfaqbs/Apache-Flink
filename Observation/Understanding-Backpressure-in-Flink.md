# Understanding Backpressure in Flink

---

## 1. Backstory

**Backstory for backpressure:**
Imagine a water pipeline:

* At the start, water (data) flows from a large tank (Kafka).
* The middle pipes (map/filter operators) are wide and can handle big flow.
* At the end, the faucet (sink = Postgres DB) is very narrow.
* Water tries to rush at 1000 liters/sec, but the faucet only lets 100 liters/sec out.
* Result: water backs up inside the pipes, pressure rises, and the tank starts holding more water.

This **backing up of flow due to a slow consumer** is what Flink calls **backpressure**.

---

## 2. Setup

* **Source (Kafka)**: producing **1000 messages/sec** across partitions.
* **Operators (map → filter)**: lightweight; they can process 1000/sec easily.
* **Sink (Postgres)**: single node, max throughput = **100 messages/sec**.
* **System resources**:

  * Kafka cluster on 3 brokers (high throughput).
  * Flink job on TaskManagers with 4 slots each.
  * Postgres sink: 1 node with limited CPU & disk I/O.

---

## 3. Step-by-step flow in Flink

1. **Sink (Postgres) processes 100/sec**

   * Postgres executes inserts at \~100/sec.
   * Sink operator in Flink batches rows into memory (its buffer).
   * Once that buffer is full and Postgres is still slow, sink stops accepting new records.

2. **Filter tries to emit 1000/sec to sink**

   * Sink’s buffers are full → filter can’t hand off data.
   * Filter operator’s thread is blocked (waiting). This is called **block on emit**.

3. **Map tries to emit 1000/sec to filter**

   * Filter is blocked, so map also cannot hand off.
   * Map threads start spending more time waiting.

4. **Source (Kafka consumer)**

   * Normally fetches 1000/sec, but now upstream is blocked.
   * Kafka consumer fetch slows to \~100/sec.
   * Kafka topic accumulates **lag** (backlog).

---

## 4. Where does the data sit?

* **Filter’s output buffer** (RAM) → full
* **Map’s output buffer** (RAM) → full
* **Flink network shuffle buffers** (between tasks) → saturated
* **Sink’s internal batch queue** (RAM before DB write) → full
* **OS TCP socket buffer** (between Flink and Postgres) → full
* **Kafka topic backlog** → grows every second

So the **extra 900/sec** are spread across these buffers and Kafka.

---

## 5. System resources under stress

### CPU

* Map/filter operators: CPU usage drops (they are waiting, not working).
* Sink operator: CPU moderate (DB I/O bound).
* Postgres node: CPU may spike if indexes/triggers are heavy.

### Memory

* Flink task memory → network/shuffle buffers swell.
* Heap pressure increases if backlogs accumulate in operator buffers.
* Postgres server memory stable (most slowdown is I/O, not RAM).

### Disk I/O

* Postgres → fsync/WAL writes become bottleneck.
* Checkpointing in Flink may also add write load.

### Network

* Flink → Postgres socket buffer fills up, packet ACKs slow down.
* Kafka → Flink consumer pulls fewer bytes/sec due to flow control.

---

## 6. Low-level explanation

1. **Buffers (RAM segments in Flink)**

   * Each operator-to-operator channel uses fixed-size buffers (\~32 KB).
   * Downstream grants credits = how many buffers upstream may send.
   * When credits drop to 0 → upstream blocks on emit.

2. **Block on emit (mechanics)**

   * Operator thread calls `output.collect(record)`.
   * Flink checks for free buffer → none available.
   * Thread is parked (not running) until credits are returned.
   * Time spent waiting = **backpressured time**.

3. **Kafka backlog (lag)**

   * Kafka producer offsets keep advancing at 1000/sec.
   * Consumer offsets advance at \~100/sec.
   * Difference = **lag**. This is monitored per partition.

4. **Propagation**

   * Sink → Filter blocked → Map blocked → Source fetch slowed.
   * Propagation is **backward** from sink to source.
   * End result: the entire pipeline matches the slowest operator.

---

## 7. Putting it all together

* At t=0, Kafka fires 1000 messages.
* Sink drains 100, holds 200 in its batch.
* Sink advertises no credits.
* Filter’s output buffers have no space → filter thread blocks on emit.
* Map’s output buffers to filter fill → map blocks on emit.
* Kafka source sees Flink not requesting enough → pulls only 100/sec.
* Kafka topic backlog grows at 900/sec.
* Flink UI shows:

  * **backpressured%** high (60–90%) at sink/filter/map.
  * **busy%** moderate/low.
  * **idle%** low.

---

## 8. ASCII picture

```
Kafka (1000/s) 
   │          (lag = growing at +900/s)
   ▼
[Source] ──buffers──▶ [map] ──buffers──▶ [filter] ──buffers──▶ [sink] ──► Postgres (100/s)
                                     ▲                         ▲
                                     └──── no credits ─────────┘
```

---

## 9. Cheat Sheet

* **Buffer** = temporary memory (RAM) holding data in transit.
* **Emit** = operator hands a record to next operator.
* **Block on emit** = operator pauses because no buffer free.
* **Backpressure** = slow sink → buffers fill → upstream blocked.
* **Backlog/lag** = Kafka messages still waiting to be consumed.

---
