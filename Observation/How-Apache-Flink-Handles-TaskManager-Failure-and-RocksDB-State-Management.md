Apache Flink is designed with strong fault-tolerance mechanisms, ensuring resilience and minimal data loss during failures. It supports advanced state backends like RocksDB with support for cloud or filesystem-based storage (e.g., Amazon S3). This document details the TaskManager failure recovery, checkpoint vs. savepoint usage, state lifecycle, and best practices when scaling TaskManagers in a distributed Flink deployment.

---

### 1. **TaskManager Failure Overview**

TaskManager is a worker node that executes job subtasks. If it fails, Flink detects the failure using a heartbeat mechanism and reschedules the tasks on available TaskManagers.

#### Failure Handling Flow:

* **Heartbeat Timeout** → JobManager marks TM as failed.
* **In-flight data** may be lost (not yet checkpointed).
* Flink restores from the latest checkpoint or savepoint.

---

### 2. **Who Stores State: JM or TM?**

* The **TaskManager** is responsible for maintaining the **runtime state** of operators during job execution.
* The **JobManager** coordinates checkpoints and holds metadata (not the actual state data).
* During checkpoints, TaskManagers persist operator state to a configured **state backend** like RocksDB, which in turn stores state to **durable storage (e.g., S3, HDFS, or local FS)**.

---

### 3. **Checkpoint vs Savepoint**

| Aspect       | Checkpoint                                  | Savepoint                                 |
| ------------ | ------------------------------------------- | ----------------------------------------- |
| Purpose      | Fault recovery                              | Manual snapshot for versioning or upgrade |
| Triggered by | Automatic (interval)                        | Manual (CLI, API)                         |
| Lifecycle    | Deleted after job finish or new one created | Persistent until manually deleted         |
| Usage        | Automatic recovery after failure            | Manual job resume or migration            |

* **Checkpoint**: Used for failure recovery. Flink uses the latest successful checkpoint to restore the job state.
* **Savepoint**: Used during controlled upgrades or migration, not deleted unless explicitly removed.

---

### 4. **Scaling TaskManagers and State Implications**

#### Scenario: Scaling Up/Down TaskManagers

* Flink redistributes tasks (and associated state) across the new parallelism.
* **Rescaling from Savepoint**: Required when changing parallelism (e.g., from 3 → 5 task slots). Savepoints support operator state redistribution.

**Key Considerations:**

* Use **savepoints** for safe rescaling.
* Ensure **state backend is durable** (e.g., S3 + RocksDB).
* Maintain **backward-compatible operator UIDs** for state restoration.

---

### 5. **State Lifecycle in File System (e.g., S3)**

| Job Status | State Outcome                                                                         |
| ---------- | ------------------------------------------------------------------------------------- |
| Running    | State resides in RocksDB on disk and uploaded to S3 (if incremental checkpoints used) |
| Canceled   | Checkpoints deleted by default (unless retention set), savepoints retained            |
| Finished   | Checkpoints deleted unless retention configured, savepoints remain                    |

**Retention Settings (Key Config):**

* `state.checkpoints.dir`: Where checkpoints are stored (e.g., `s3://my-bucket/flink/checkpoints`)
* `state.savepoints.dir`: Where savepoints are manually triggered to store
* `state.backend.rocksdb.checkpoint.transfer.thread.num`: Control parallelism for state uploads
* `state.checkpoints.cleanup.policy`: Controls post-job cleanup (e.g., retain on cancel)

---

### 6. **Best Practices**

* Always use **savepoints** when stopping jobs that will be restarted/rescaled.
* For production, configure a **durable state backend** like RocksDB with **incremental checkpoints**.
* Retain state using `state.checkpoints.cleanup.policy=RETAIN_ON_CANCELLATION` if post-job analysis or reprocessing is needed.
* Ensure **unique operator UIDs** in code so Flink can map state during restarts or job upgrades.
* Avoid state loss by using `HA mode` and `durable backends` (e.g., S3).

---

### Summary Table

| Component     | Role                                                |
| ------------- | --------------------------------------------------- |
| JobManager    | Coordinates checkpoints, holds metadata             |
| TaskManager   | Executes operators, manages local state             |
| RocksDB       | Local state storage backend per TM                  |
| S3/FileSystem | Durable remote store for checkpoint/savepoint state |

---

### References

* [Flink Docs: Fault Tolerance](https://nightlies.apache.org/flink/flink-docs-release-1.19/docs/ops/state/checkpoints/)
* [State Backends in Flink](https://nightlies.apache.org/flink/flink-docs-release-1.19/docs/ops/state/state_backends/)
* [Savepoints and Rescaling](https://nightlies.apache.org/flink/flink-docs-release-1.19/docs/ops/state/savepoints/)
