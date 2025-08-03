#  Flink Checkpointing: Full Guide

---

## 🎯 What is a checkpoint (with analogy)?

**Analogy:**
Imagine you're writing a long school essay. Every 10 minutes, you click "Save" on your file.
If your laptop crashes, you don't start from zero — you reopen the last saved version and continue.

> In Flink, a **checkpoint** is that periodic "Save" — it stores the **state of the job** and its progress so it can **recover** after failure.

---

## 🧠 Why was checkpointing introduced?

* Streaming jobs run **forever**
* Things **crash**: hardware, JVM, network
* Without checkpoints, everything would start from scratch
* Checkpoints allow Flink to **restore the job** **automatically** — **from the exact point it was** (state + offsets)

> ✅ Ensures correctness and continuity in case of failure

---

## ⚙️ How checkpointing works (simple flow):

1. Flink triggers a checkpoint (based on time or event)
2. Each operator **writes its state to a storage backend** (e.g., filesystem, S3, HDFS)
3. Once all operators confirm the checkpoint → Flink marks it as **complete**
4. If a crash occurs → Flink **restores the job** from the **latest completed checkpoint**

---

## 📂 Checkpoint Configs from `flink-conf.yaml` (Explained)

Let's go line-by-line through the config block you shared.

---

### 🔧 `execution.checkpointing.interval`

```yaml
execution.checkpointing.interval: 3min
```

\| Description | Flink will **trigger a checkpoint every 3 minutes** |
\| Can be set in code? | ✅ Yes, via `env.enableCheckpointing(180_000)` |
\| Options | Any time format (e.g., `30s`, `5min`, etc.) |
\| Ideal value | `30s` to `5min`, based on job size and recovery needs |
\| If not set or set to 0 | Checkpointing is disabled |
\| Notes | **Must be > 0** to turn on checkpointing |

---

### 🔧 `execution.checkpointing.mode`

```yaml
mode: [EXACTLY_ONCE, AT_LEAST_ONCE]
```

\| Description | Guarantees for state and message processing |
\| Can be set in code? | ✅ Yes (`CheckpointingMode.EXACTLY_ONCE`) |
\| Options | `EXACTLY_ONCE` (default), `AT_LEAST_ONCE` |
\| Ideal value | `EXACTLY_ONCE` for accuracy; `AT_LEAST_ONCE` for speed |
\| If not set | Flink defaults to `EXACTLY_ONCE` |

---

### 🔧 `execution.checkpointing.externalized-checkpoint-retention`

```yaml
externalized-checkpoint-retention: [DELETE_ON_CANCELLATION, RETAIN_ON_CANCELLATION]
```

\| Description | What happens to checkpoints when the job is cancelled |
\| Can be set in code? | ✅ Yes (`ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION`) |
\| Options |

* `RETAIN_ON_CANCELLATION` ✅ keep for manual recovery
* `DELETE_ON_CANCELLATION` ❌ delete checkpoints when job is cancelled |
  \| Ideal value | `RETAIN_ON_CANCELLATION` in prod |
  \| Notes | Needed to allow **manual job restarts** from a checkpoint |

---

### 🔧 `execution.checkpointing.max-concurrent-checkpoints`

```yaml
max-concurrent-checkpoints: 1
```

\| Description | How many checkpoints can run at the same time |
\| Can be set in code? | ✅ Yes |
\| Options | `1` (safe) or more |
\| Ideal value | `1` for stable jobs; `2–3` for fast, state-light jobs |
\| Notes | Avoid overlapping checkpoints unless needed |

---

### 🔧 `execution.checkpointing.min-pause`

```yaml
min-pause: 0
```

\| Description | Minimum wait between two checkpoints |
\| Can be set in code? | ✅ Yes |
\| Options | Any time duration |
\| Ideal value | `500ms`–`2s` for stable pacing |
\| Notes | Prevents checkpoint storms during retries |

---

### 🔧 `execution.checkpointing.timeout`

```yaml
timeout: 10min
```

\| Description | How long to wait before a checkpoint is considered failed |
\| Can be set in code? | ✅ Yes |
\| Ideal value | `1–10min`, depends on state size |
\| Notes | Larger jobs need longer timeouts |

---

### 🔧 `execution.checkpointing.tolerable-failed-checkpoints`

```yaml
tolerable-failed-checkpoints: 0
```

\| Description | Number of checkpoints that can fail before job fails |
\| Can be set in code? | ✅ Yes |
\| Options | `0`, `1`, `2`, etc. |
\| Ideal value | `0` (strict), `1+` (lenient jobs) |
\| Notes | Helps when storage is flaky temporarily |

---

### 🔧 `execution.checkpointing.unaligned`

```yaml
unaligned: false
```

\| Description | Helps checkpoints complete faster under backpressure |
\| Can be set in code? | ✅ Yes (advanced) |
\| Ideal value | `false` (safe default); `true` if you hit checkpoint delays |
\| Notes | For **heavy pipelines** with network delay |

---

## 📦 State Backend Configs

---

### 🔧 `state.backend.type`

```yaml
state.backend.type: hashmap
```

\| Description | Defines how and where Flink stores operator state |
\| Options |

* `hashmap` (in-memory) ✅ fast, but small jobs
* `rocksdb` (on-disk) ✅ large state, scalable
  \| Ideal value | `hashmap` for dev, `rocksdb` for production |
  \| Notes | RocksDB supports **incremental checkpoints**

---

### 🔧 `state.backend.incremental`

```yaml
state.backend.incremental: false
```

\| Description | Enables RocksDB to save **only changed data** in checkpoints |
\| Can be set in code? | ✅ Yes |
\| Ideal value | `true` when using `rocksdb` |
\| Notes | Saves time and storage for large jobs

---

### 🔧 `state.checkpoints.dir`

```yaml
state.checkpoints.dir: hdfs://namenode-host:port/flink-checkpoints
```

\| Description | Where checkpoints are saved |
\| Options | Any shared file system: HDFS, S3, NFS |
\| Ideal value | `s3://your-bucket/checkpoints/` or HDFS path |
\| Notes | This path must be durable and reachable from **all nodes**

---

## 🗃️ Savepoints – What, Why, How

---

### 🎯 What is a savepoint?

**Analogy:**
Checkpoint = auto-save
Savepoint = manual "Save As"

🧠 Savepoints are **manual, controlled snapshots** of your job’s state:

* Used to **migrate jobs**
* Used to **restart from a known safe state**
* Used to **pause/resume** jobs cleanly

> They are **triggered manually**, not automatic like checkpoints

---

### 🔧 `state.savepoints.dir`

```yaml
state.savepoints.dir: hdfs://namenode-host:port/flink-savepoints
```

\| Description | Where savepoints are stored |
\| Options | HDFS, S3, etc. |
\| Ideal value | A separate bucket or directory from checkpoints |
\| Notes | Path must be accessible from **JobManager and TaskManagers**

---

## ✅ Summary Table

| Feature         | Checkpoint              | Savepoint                         |
| --------------- | ----------------------- | --------------------------------- |
| Auto or manual? | Automatic (by interval) | Manual (triggered by user)        |
| Purpose         | Fault recovery          | Job upgrades, controlled restarts |
| Speed           | Faster                  | Slower                            |
| Cleanup         | Auto-deleted (optional) | Persistent by default             |
| Triggers        | Config or code          | CLI or REST API                   |
| Path            | `state.checkpoints.dir` | `state.savepoints.dir`            |

# ✅ Deep Dive: Checkpointing with Job Example (5 Operators eg)

---

## 🧱 First — What is an Operator in Flink?

> An **operator** is **one transformation step** in a Flink job — like `map`, `filter`, `keyBy`, `window`, `sum`, `sink`, etc.

Flink turns every operator into:

* 1 or more **tasks** (via parallelism)
* Each of them will hold and checkpoint their own **state**

---

## 🧪 Real Job Example (5 Operators)

Here’s a job that reads user clicks from Kafka, filters logged-in users, maps them to (userID, 1), groups them by ID, and writes click counts to PostgreSQL:

```java
DataStream<String> stream = env.addSource(kafkaSource);      // Operator 1

stream.filter(user -> user.isLoggedIn())                      // Operator 2
      .map(user -> Tuple2.of(user.id, 1))                     // Operator 3
      .keyBy(t -> t.f0)                                       // Operator 4 (with keyBy)
      .sum(1)                                                 // (still Operator 4, stateful)
      .addSink(postgresSink);                                 // Operator 5
```

### 💡 Operators Breakdown:

| Operator      | What It Does                      | Stateful?       | Stores Checkpointed State? |
| ------------- | --------------------------------- | --------------- | -------------------------- |
| `source`      | Reads data from Kafka             | ✅ yes (offsets) | ✅ yes                      |
| `filter`      | Filters only logged-in users      | ❌ stateless     | ❌ no                       |
| `map`         | Transforms each user to `(id, 1)` | ❌ stateless     | ❌ no                       |
| `keyBy + sum` | Aggregates clicks per user        | ✅ stateful      | ✅ yes                      |
| `sink`        | Writes to PostgreSQL              | ✅ depends       | ✅ if exactly-once used     |

---

## 📷 How Checkpointing Works in This Job

1. Every **3 minutes** (or set interval), Flink starts a **checkpoint**
2. It tells all operators: “Prepare to snapshot your state”
3. Each **task** that holds state does:

   * Save Kafka **offsets** (source)
   * Save **per-user count** state (keyBy + sum)
   * Confirm “done” to Flink
4. Flink writes all of that to the `checkpoints.dir` (e.g., HDFS/S3)
5. If job crashes later, it **reloads the state** from that last completed checkpoint

---

## ⚙️ How Configs Affect This Example

Let’s now revisit key configs from this job’s point of view.

---

### 🔧 `execution.checkpointing.interval: 3min`

Flink checkpoints this job **every 3 minutes**.
This means:

* Kafka offsets saved (you won’t re-read the same records)
* Per-user click counts (in sum) are stored

> ✅ If the job crashes, it resumes from **the last 3-minute point**

---

### 🔧 `execution.checkpointing.mode: EXACTLY_ONCE`

This guarantees:

* **No data loss**
* **No duplication**
* Sink will **only write records once** (important for billing, payments, etc.)

> Internally, Flink aligns checkpointing with sink commits

---

### 🔧 `externalized-checkpoint-retention: RETAIN_ON_CANCELLATION`

Even if you stop this job:

* The checkpoint **is kept**
* You can **manually restart** from that point later

---

### 🔧 `state.checkpoints.dir`

Set to:

```yaml
state.checkpoints.dir: hdfs://namenode/flink-checkpoints
```

All of this job's:

* Kafka offsets
* Sum operator’s keyed state
  … get saved to that folder every 3 minutes

> ✅ Shared between all JobManagers and TaskManagers

---

### 🔧 `state.backend.type: rocksdb`

If this job handles:

* **millions of users**
* Huge per-user click state

You switch to RocksDB backend so that:

* State lives **on disk**, not just in memory
* State can scale large (with incremental checkpointing enabled)

---

## 🔍 Optional Enhancements (for this job)

| Use Case                              | Config                                    |
| ------------------------------------- | ----------------------------------------- |
| Speed up large checkpoints            | `execution.checkpointing.unaligned: true` |
| Avoid backpressure during checkpoint  | `min-pause: 1s`                           |
| Save disk by skipping unchanged state | `state.backend.incremental: true`         |

---

## ✅ Summary Recap for This Job (5 Operators)

| Component                     | Role in Checkpointing                          |
| ----------------------------- | ---------------------------------------------- |
| **source (Kafka)**            | Saves current offsets ✅                        |
| **filter + map**              | Stateless; nothing to save ❌                   |
| **keyBy + sum**               | Saves per-user state ✅                         |
| **sink (PostgreSQL)**         | Uses checkpoint barriers to avoid duplicates ✅ |
| **Checkpoint dir**            | Where all saved state goes (e.g., S3, HDFS)    |
| **Backend (rocksdb/hashmap)** | Controls whether state is in memory or disk    |