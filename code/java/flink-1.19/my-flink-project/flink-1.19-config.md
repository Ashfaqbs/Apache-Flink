# Apache Flink 1.19 — Configuration Reference

Source: https://nightlies.apache.org/flink/flink-docs-release-1.19/docs/deployment/config/

> **Config file:** As of v1.19, the default config file is `config.yaml`.
> The legacy `flink-conf.yaml` is deprecated and will be removed in v2.0.

---

## Table of Contents

1. [Basic Setup — Hosts & Ports](#1-basic-setup--hosts--ports)
2. [Fault Tolerance](#2-fault-tolerance)
   - [Restart Strategies](#restart-strategies)
   - [Retryable Cleanup](#retryable-cleanup)
3. [Checkpoints & State Backends](#3-checkpoints--state-backends)
4. [High Availability](#4-high-availability)
5. [Memory Configuration](#5-memory-configuration)
   - [JobManager Memory](#jobmanager-memory)
   - [TaskManager Memory](#taskmanager-memory)
6. [Execution](#6-execution)
7. [Security](#7-security)
   - [SSL](#ssl)
   - [Delegation Tokens](#delegation-tokens)
   - [ZooKeeper Auth](#zookeeper-authentication)
   - [Kerberos](#kerberos-authentication)
8. [Metrics](#8-metrics)
9. [History Server](#9-history-server)
10. [Resource Orchestration](#10-resource-orchestration)
    - [YARN](#yarn)
    - [Kubernetes](#kubernetes)
11. [Debugging & Expert Tuning](#11-debugging--expert-tuning)
12. [JVM & Logging](#12-jvm--logging)
13. [Experimental](#13-experimental)
14. [Miscellaneous](#14-miscellaneous)
15. [Deprecated Options](#15-deprecated-options)

---

## 1. Basic Setup — Hosts & Ports

These control where the JobManager and TaskManagers bind and listen. Critical for networking in both local and cluster deployments.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `jobmanager.bind-host` | (none) | String | Local network interface the JobManager binds to. Defaults to `0.0.0.0` if unset (binds all interfaces). Set this for multi-NIC hosts. |
| `jobmanager.rpc.address` | (none) | String | External network address for JobManager RPC. Used by TaskManagers and clients to reach the JM. Required in standalone mode. |
| `jobmanager.rpc.port` | 6123 | Integer | Port for JobManager RPC communication. This is what TaskManagers connect to. |
| `jobmanager.rpc.bind-port` | (none) | Integer | Local port the JM RPC binds to. Useful when the bind port differs from the advertised port (e.g., Docker port mapping). |
| `rest.address` | (none) | String | Address clients use to reach the REST API / Web UI. If not set, falls back to `jobmanager.rpc.address`. |
| `rest.bind-address` | (none) | String | Address the REST server binds to locally. Defaults to `0.0.0.0` if unset. |
| `rest.port` | 8081 | Integer | REST API port clients connect to. |
| `rest.bind-port` | "8081" | String | Port(s) the REST server listens on. Accepts ranges like `"8080-8090"` or comma-separated lists. |
| `rest.path` | (none) | String | Base path for REST endpoints (e.g., `/flink`). Useful when running behind a reverse proxy. |
| `taskmanager.host` | (none) | String | External hostname/IP the TaskManager advertises. Must be reachable by the JobManager. |
| `taskmanager.bind-host` | (none) | String | Local network interface TaskManager binds to. Defaults to `0.0.0.0`. |
| `taskmanager.rpc.port` | "0" | String | External RPC port for the TaskManager. `0` means OS assigns a random free port. |
| `taskmanager.rpc.bind-port` | (none) | Integer | Local port the TaskManager RPC binds to. |
| `taskmanager.data.port` | 0 | Integer | Port for data exchange between TaskManagers. `0` = OS-assigned. |
| `taskmanager.data.bind-port` | (none) | String | Local binding port for data exchange. |
| `taskmanager.collect-sink.port` | 0 | Integer | Port used to retrieve collect query results from TaskManagers. |
| `metrics.internal.query-service.port` | "0" | String | Port range for the internal metrics query service. |

---

## 2. Fault Tolerance

### Restart Strategies

Controls how Flink restarts a job after failure. Choose based on your tolerance for downtime vs. repeated failures.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `restart-strategy.type` | (none) | String | Which strategy to use: `disable` (no restart), `fixed-delay`, `failure-rate`, or `exponential-delay`. If not set, Flink uses `fixed-delay` when checkpointing is on, otherwise `disable`. |

#### Fixed Delay

Retries a fixed number of times, waiting the same duration between each attempt.

```yaml
restart-strategy.type: fixed-delay
restart-strategy.fixed-delay.attempts: 3
restart-strategy.fixed-delay.delay: 10s
```

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `restart-strategy.fixed-delay.attempts` | 1 | Integer | How many times to restart before declaring the job failed. |
| `restart-strategy.fixed-delay.delay` | 1s | Duration | Wait time between consecutive restart attempts. Avoids hammering a flapping resource. |

#### Failure Rate

Allows restarts as long as the failure rate stays below a threshold over a sliding time window.

```yaml
restart-strategy.type: failure-rate
restart-strategy.failure-rate.max-failures-per-interval: 5
restart-strategy.failure-rate.failure-rate-interval: 5min
restart-strategy.failure-rate.delay: 10s
```

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `restart-strategy.failure-rate.max-failures-per-interval` | 1 | Integer | Maximum number of restarts allowed within the interval window. |
| `restart-strategy.failure-rate.failure-rate-interval` | 1min | Duration | Sliding time window for counting failures. |
| `restart-strategy.failure-rate.delay` | 1s | Duration | Delay between consecutive restart attempts. |

#### Exponential Delay

Doubles (or multiplies by `backoff-multiplier`) the wait between restarts up to a max, with optional jitter. Best for production — avoids thundering herd restarts.

```yaml
restart-strategy.type: exponential-delay
restart-strategy.exponential-delay.initial-backoff: 1s
restart-strategy.exponential-delay.max-backoff: 1min
restart-strategy.exponential-delay.backoff-multiplier: 2.0
restart-strategy.exponential-delay.jitter-factor: 0.1
```

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `restart-strategy.exponential-delay.initial-backoff` | 1s | Duration | Starting wait time before the first restart attempt. |
| `restart-strategy.exponential-delay.max-backoff` | 1min | Duration | Maximum delay cap — backoff will not grow beyond this. |
| `restart-strategy.exponential-delay.backoff-multiplier` | 1.5 | Double | Factor by which the backoff grows after each failure. |
| `restart-strategy.exponential-delay.jitter-factor` | 0.1 | Double | Adds random noise to backoff to prevent synchronized restarts across tasks. |
| `restart-strategy.exponential-delay.reset-backoff-threshold` | 1h | Duration | If a job runs successfully for this long, the backoff resets to initial value. |
| `restart-strategy.exponential-delay.attempts-before-reset-backoff` | infinite | Integer | Number of successful restarts before backoff resets. |

---

### Retryable Cleanup

Controls how Flink retries cleanup operations (e.g., deleting checkpoint files) after a job completes or fails.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `cleanup-strategy.type` | "exponential-delay" | String | Cleanup retry strategy: `none`, `fixed-delay`, or `exponential-delay`. |

#### Fixed-Delay Cleanup

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `cleanup-strategy.fixed-delay.attempts` | infinite | Integer | Max cleanup retry attempts before giving up. |
| `cleanup-strategy.fixed-delay.delay` | 1min | Duration | Wait between cleanup retries. |

#### Exponential-Delay Cleanup

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `cleanup-strategy.exponential-delay.attempts` | infinite | Integer | Max retries before giving up on cleanup. |
| `cleanup-strategy.exponential-delay.initial-backoff` | 1s | Duration | Starting wait between cleanup retries. |
| `cleanup-strategy.exponential-delay.max-backoff` | 1h | Duration | Maximum wait between cleanup retries. |

---

## 3. Checkpoints & State Backends

Checkpointing enables fault tolerance by periodically snapshotting job state. State backends control where/how state is stored.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `state.backend.type` | "hashmap" | String | State backend: `hashmap` (in-heap, fast, limited by JVM heap), `rocksdb` (disk-backed, large state, slower), or a fully-qualified class name. Use `rocksdb` for large state (GB+). |
| `state.checkpoint-storage` | (none) | String | Where checkpoints are persisted: `jobmanager` (in-memory, no HA), `filesystem` (durable), or a class name. Always use `filesystem` in production. |
| `state.checkpoints.dir` | (none) | String | URI for checkpoint storage (e.g., `hdfs:///flink/checkpoints` or `s3://bucket/checkpoints`). Required when using filesystem storage. |
| `state.savepoints.dir` | (none) | String | Default directory for savepoints when no explicit path is given during a `savepoint` command. |
| `state.backend.incremental` | false | Boolean | Enable incremental checkpoints (only supported by RocksDB backend). Dramatically reduces checkpoint size for large state — enable it for RocksDB. |
| `state.backend.local-recovery` | false | Boolean | Keep a local copy of checkpoint state on TaskManagers. Speeds up recovery by avoiding remote reads — good for large state. |
| `state.checkpoint.cleaner.parallel-mode` | true | Boolean | Discard old checkpoint files in parallel. Keep enabled for faster cleanup of large checkpoints. |
| `state.checkpoints.num-retained` | 1 | Integer | How many completed checkpoints to keep on disk. More retained = safer recovery, but more storage. Typical: 2–3. |
| `taskmanager.state.local.root-dirs` | (none) | String | Local directories on TaskManagers for storing local recovery state. Required when `state.backend.local-recovery` is enabled. |

---

## 4. High Availability

HA allows the cluster to survive JobManager failures. Requires an external coordination service (ZooKeeper or Kubernetes).

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `high-availability.type` | "NONE" | String | HA mode: `NONE` (no HA), `ZOOKEEPER`, `KUBERNETES`, or a fully-qualified class name. |
| `high-availability.cluster-id` | "/default" | String | Unique identifier for this Flink cluster in the HA backend. Use different IDs if running multiple clusters against the same ZK. |
| `high-availability.storageDir` | (none) | String | Durable file system path for HA metadata (job graphs, completed checkpoints pointers). Must be accessible from all nodes (HDFS, S3, etc.). |

### JobResultStore

Tracks completed job results for exactly-once cleanup guarantees.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `job-result-store.delete-on-commit` | true | Boolean | Automatically delete job result entries after successful cleanup. Disable if you want to audit completed jobs. |
| `job-result-store.storage-path` | (none) | String | File system path for job result storage. Defaults to a subdirectory of `high-availability.storageDir`. |

### ZooKeeper HA

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `high-availability.zookeeper.quorum` | (none) | String | Comma-separated list of ZooKeeper hosts (e.g., `zk1:2181,zk2:2181,zk3:2181`). Required for ZK-based HA. |
| `high-availability.zookeeper.path.root` | "/flink" | String | Root ZooKeeper znode under which all Flink HA data is stored. Useful for namespace isolation. |

---

## 5. Memory Configuration

Flink has a detailed memory model. Wrong settings are the most common cause of OOM errors. Always set memory explicitly in production.

### JobManager Memory

The JobManager runs the scheduler, checkpoint coordinator, and REST server. It's generally lightweight — 1–2 GB process memory is typical.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `jobmanager.memory.process.size` | (none) | MemorySize | **Total JVM process memory** for the JM. Sets overall budget. Example: `1600m`. Usually the only setting you need to change. |
| `jobmanager.memory.flink.size` | (none) | MemorySize | Total Flink memory (heap + off-heap), excluding JVM Metaspace and JVM Overhead. Alternative to `process.size`. |
| `jobmanager.memory.heap.size` | (none) | MemorySize | JVM heap size. Minimum 128 MB recommended. |
| `jobmanager.memory.off-heap.size` | 128mb | MemorySize | Off-heap memory for direct/native allocations (e.g., Netty buffers). |
| `jobmanager.memory.jvm-metaspace.size` | 256mb | MemorySize | JVM Metaspace — holds class metadata. Increase if you see `OutOfMemoryError: Metaspace`. |
| `jobmanager.memory.jvm-overhead.fraction` | 0.1 | Float | Fraction of total process memory reserved for JVM overhead (GC, JIT, threads). |
| `jobmanager.memory.jvm-overhead.min` | 192mb | MemorySize | Minimum JVM overhead size. |
| `jobmanager.memory.jvm-overhead.max` | 1gb | MemorySize | Maximum JVM overhead size. |
| `jobmanager.memory.enable-jvm-direct-memory-limit` | false | Boolean | Enforce `-XX:MaxDirectMemorySize` on the JM JVM. Enable if you want strict direct memory control. |

### TaskManager Memory

TaskManagers run your actual operators. Memory sizing here directly affects throughput and stability.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `taskmanager.memory.process.size` | (none) | MemorySize | **Total TM process memory**. Example: `4096m`. Usually the primary setting — set this and let Flink derive the rest. |
| `taskmanager.memory.flink.size` | (none) | MemorySize | Total Flink memory (task heap + managed + network + framework), excluding JVM overhead. Alternative to `process.size`. |
| `taskmanager.memory.task.heap.size` | (none) | MemorySize | Heap memory available to user code (your operators). Remaining heap after framework heap is allocated. |
| `taskmanager.memory.managed.size` | (none) | MemorySize | Explicit size for managed memory (used by RocksDB, batch sort, Python). Overrides `managed.fraction`. |
| `taskmanager.memory.managed.fraction` | 0.4 | Float | Fraction of Flink memory allocated as managed memory. Default 40% — reduce if you need more task heap. |
| `taskmanager.memory.managed.consumer-weights` | OPERATOR:70, STATE_BACKEND:70, PYTHON:30 | Map | Relative weights for managed memory consumers. Adjust if mixing RocksDB + Python in the same TM. |
| `taskmanager.memory.network.fraction` | 0.1 | Float | Fraction of Flink memory for network buffers. Increase for high-throughput pipelines with many channels. |
| `taskmanager.memory.network.min` | 64mb | MemorySize | Minimum network buffer memory. |
| `taskmanager.memory.network.max` | infinite | MemorySize | Maximum network buffer memory cap. |
| `taskmanager.memory.framework.heap.size` | 128mb | MemorySize | Heap reserved for Flink framework internals (not user code). Do not reduce. |
| `taskmanager.memory.framework.off-heap.size` | 128mb | MemorySize | Off-heap memory for Flink framework use. |
| `taskmanager.memory.framework.off-heap.batch-shuffle.size` | 64mb | MemorySize | Memory for reading batch shuffle data from disk. |
| `taskmanager.memory.task.off-heap.size` | 0 bytes | MemorySize | Off-heap memory reserved for user operator code (e.g., JNI allocations). |
| `taskmanager.memory.jvm-metaspace.size` | 256mb | MemorySize | JVM Metaspace for class metadata. Increase for jobs with many classes (complex UDFs, heavy libraries). |
| `taskmanager.memory.jvm-overhead.fraction` | 0.1 | Float | Fraction of total process memory reserved for JVM overhead. |
| `taskmanager.memory.jvm-overhead.min` | 192mb | MemorySize | Minimum JVM overhead. |
| `taskmanager.memory.jvm-overhead.max` | 1gb | MemorySize | Maximum JVM overhead. |

---

## 6. Execution

### Checkpointing (via code or config)

These can also be set programmatically via `StreamExecutionEnvironment`, but config-file values act as defaults.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `execution.checkpointing.interval` | (none) | Duration | Checkpoint trigger interval. Setting this **enables checkpointing**. Example: `30s`. |
| `execution.checkpointing.mode` | `EXACTLY_ONCE` | Enum | Delivery guarantee: `EXACTLY_ONCE` (requires aligned barriers, higher latency) or `AT_LEAST_ONCE` (faster, may reprocess). |
| `execution.checkpointing.timeout` | 10min | Duration | Maximum time a checkpoint attempt can take before it is aborted. Reduce if slow checkpoints are masking issues. |
| `execution.checkpointing.max-concurrent-checkpoints` | 1 | Integer | Maximum number of checkpoint attempts that can run in parallel. Keep at 1 unless you have a specific reason. |

### Pipeline / General Execution

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `execution.runtime-mode` | `STREAMING` | Enum | `STREAMING` for unbounded streams, `BATCH` for bounded inputs, `AUTOMATIC` lets Flink decide. |
| `execution.attached` | false | Boolean | If `true`, the client waits for the job to finish before returning. Useful for scripted pipelines. |

---

## 7. Security

### SSL

Encrypt Flink's internal and REST communications. Internal = JM↔TM data plane; REST = Web UI and API.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `security.ssl.internal.enabled` | false | Boolean | Enable SSL/TLS for internal cluster communication (RPC, data exchange). |
| `security.ssl.internal.keystore` | (none) | String | Path to the Java keystore file for internal TLS endpoints. |
| `security.ssl.internal.keystore-password` | (none) | String | Password to decrypt the internal keystore. |
| `security.ssl.internal.key-password` | (none) | String | Password for the private key within the internal keystore. |
| `security.ssl.internal.truststore` | (none) | String | Path to truststore containing trusted CA certificates for internal communication. |
| `security.ssl.internal.truststore-password` | (none) | String | Password for the internal truststore. |
| `security.ssl.internal.cert.fingerprint` | (none) | String | SHA1 fingerprint of the internal certificate for pinning (optional extra validation). |
| `security.ssl.rest.enabled` | false | Boolean | Enable SSL for the REST API and Web UI. |
| `security.ssl.rest.authentication-enabled` | false | Boolean | Require mutual TLS (mTLS) for REST clients — clients must also present a certificate. |
| `security.ssl.rest.keystore` | (none) | String | Keystore for REST TLS. |
| `security.ssl.rest.keystore-password` | (none) | String | Keystore password for REST TLS. |
| `security.ssl.rest.key-password` | (none) | String | Key password within the REST keystore. |
| `security.ssl.rest.truststore` | (none) | String | Truststore for REST TLS (used when mTLS is enabled). |
| `security.ssl.rest.truststore-password` | (none) | String | Truststore password for REST. |
| `security.ssl.rest.cert.fingerprint` | (none) | String | SHA1 certificate fingerprint for REST endpoint (optional pinning). |
| `security.ssl.protocol` | "TLSv1.2" | String | TLS protocol version. Consider `TLSv1.3` for better security on supported JDKs. |
| `security.ssl.algorithms` | "TLS_RSA_WITH_AES_128_CBC_SHA" | String | Comma-separated list of allowed cipher suites. |
| `security.ssl.verify-hostname` | true | Boolean | Verify hostname during TLS handshake. Do not disable in production. |

### Delegation Tokens

Used in Hadoop/YARN/Kerberos environments for propagating credentials to tasks.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `security.delegation.tokens.enabled` | true | Boolean | Master switch for the delegation token system. Disable only in non-Kerberos environments. |
| `security.delegation.tokens.renewal.time-ratio` | 0.75 | Double | Fraction of token lifetime at which Flink re-obtains a new token. 0.75 means renew when 75% of lifetime elapsed. |
| `security.delegation.tokens.renewal.retry.backoff` | 1h | Duration | Wait time before retrying a failed token renewal. |
| `security.delegation.token.provider.<serviceName>.enabled` | true | Boolean | Enable/disable token provider for a specific service (e.g., `hdfs`, `hbase`). |

### ZooKeeper Authentication

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `zookeeper.sasl.disable` | false | Boolean | Disable SASL authentication for ZooKeeper. Only disable in fully trusted networks. |
| `zookeeper.sasl.service-name` | "zookeeper" | String | Service name used during SASL negotiation. Must match ZK server config. |
| `zookeeper.sasl.login-context-name` | "Client" | String | JAAS login context name for ZooKeeper client auth. Must exist in your JAAS config file. |

### Kerberos Authentication

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `security.kerberos.login.principal` | (none) | String | Kerberos principal (e.g., `flink/_HOST@REALM`). |
| `security.kerberos.login.keytab` | (none) | String | Absolute path to the Kerberos keytab file. |
| `security.kerberos.login.use-ticket-cache` | true | Boolean | Read credentials from Kerberos ticket cache (in addition to or instead of keytab). |
| `security.kerberos.login.contexts` | (none) | String | Comma-separated JAAS login context names to log into (e.g., `Client,KafkaClient`). |
| `security.kerberos.relogin.period` | 1min | Duration | How often Flink re-logins with the keytab to keep credentials fresh. |
| `security.kerberos.access.hadoopFileSystems` | (none) | List | Semicolon-separated list of Kerberos-secured HDFS URIs to obtain tokens for. |

---

## 8. Metrics

Flink's metrics system is pluggable. You configure reporters to push metrics to external systems (Prometheus, JMX, InfluxDB, etc.).

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `metrics.reporters` | (none) | String | Comma-separated list of named reporter instances (e.g., `prometheus, jmx`). |
| `metrics.reporter.<name>.factory.class` | (none) | String | Fully-qualified class of the reporter factory for reporter named `<name>`. |
| `metrics.reporter.<name>.interval` | 10s | Duration | How often the reporter sends metrics to the backend. |
| `metrics.reporter.<name>.<option>` | (none) | String | Reporter-specific options (e.g., `metrics.reporter.prometheus.port: 9249`). |
| `metrics.scope.jm` | `<host>.jobmanager` | String | Scope format string for JobManager metrics. |
| `metrics.scope.jm.job` | `<host>.jobmanager.<job_name>` | String | Scope format string for JM-level job metrics. |
| `metrics.scope.tm` | `<host>.taskmanager.<tm_id>` | String | Scope format string for TaskManager metrics. |
| `metrics.scope.tm.job` | `<host>.taskmanager.<tm_id>.<job_name>` | String | Scope format for TM-level job metrics. |
| `metrics.scope.task` | `<host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>` | String | Scope format for individual task metrics. |
| `metrics.scope.operator` | `<host>.taskmanager.<tm_id>.<job_name>.<operator_name>.<subtask_index>` | String | Scope format for operator-level metrics. |
| `metrics.scope.delimiter` | "." | String | Delimiter between scope components in metric names. |
| `metrics.latency.history-size` | 128 | Integer | Number of latency samples kept per operator. |
| `metrics.latency.interval` | 0 (disabled) | Long | Interval (ms) for latency tracking between sources and sinks. 0 = disabled. Enable with caution — has overhead. |
| `metrics.latency.granularity` | OPERATOR | Enum | Granularity of latency tracking: `SINGLE`, `OPERATOR`, or `SUBTASK`. |
| `metrics.internal.query-service.port` | "0" | String | Port for the internal metrics HTTP query service. |
| `metrics.system-resource` | false | Boolean | Enable system-level resource metrics (CPU, memory, network I/O from the OS). |
| `metrics.system-resource-probing-interval` | 5000 | Long | Interval (ms) for probing system resource metrics. |

### RocksDB Native Metrics

These expose RocksDB internal counters through Flink's metrics system.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `state.backend.rocksdb.metrics.cur-size-active-mem-table` | false | Boolean | Expose active MemTable size metric. |
| `state.backend.rocksdb.metrics.cur-size-all-mem-tables` | false | Boolean | Expose total MemTable size (active + unflushed). |
| `state.backend.rocksdb.metrics.size-all-mem-tables` | false | Boolean | Expose size of all MemTables including pinned. |
| `state.backend.rocksdb.metrics.num-entries-active-mem-table` | false | Boolean | Expose number of entries in the active MemTable. |
| `state.backend.rocksdb.metrics.num-entries-imm-mem-tables` | false | Boolean | Expose entries in immutable MemTables. |
| `state.backend.rocksdb.metrics.num-deletes-active-mem-table` | false | Boolean | Expose delete entries in active MemTable. |
| `state.backend.rocksdb.metrics.num-deletes-imm-mem-tables` | false | Boolean | Expose delete entries in immutable MemTables. |
| `state.backend.rocksdb.metrics.estimate-num-keys` | false | Boolean | Expose estimated number of keys in RocksDB. |
| `state.backend.rocksdb.metrics.estimate-live-data-size` | false | Boolean | Expose estimated live data size. |
| `state.backend.rocksdb.metrics.total-sst-files-size` | false | Boolean | Expose total SST file size on disk. |
| `state.backend.rocksdb.metrics.live-sst-files-size` | false | Boolean | Expose live SST file size. |
| `state.backend.rocksdb.metrics.block-cache-capacity` | false | Boolean | Expose block cache capacity. |
| `state.backend.rocksdb.metrics.block-cache-usage` | false | Boolean | Expose block cache current usage. |
| `state.backend.rocksdb.metrics.block-cache-pinned-usage` | false | Boolean | Expose pinned block cache memory usage. |
| `state.backend.rocksdb.metrics.column-family-as-variable` | false | Boolean | Report column family name as a metric variable rather than appending it to the metric name. |
| `state.backend.rocksdb.metrics.compaction-pending` | false | Boolean | Expose whether compaction is pending (1=yes, 0=no). |
| `state.backend.rocksdb.metrics.num-running-compactions` | false | Boolean | Expose number of active compactions. |
| `state.backend.rocksdb.metrics.num-running-flushes` | false | Boolean | Expose number of active flushes. |
| `state.backend.rocksdb.metrics.actual-delayed-write-rate` | false | Boolean | Expose current write throttle rate. |
| `state.backend.rocksdb.metrics.is-write-stopped` | false | Boolean | Expose whether writes are stopped due to compaction. |
| `state.backend.rocksdb.metrics.mem-table-flush-pending` | false | Boolean | Expose whether a MemTable flush is pending. |
| `state.backend.rocksdb.metrics.num-immutable-mem-table` | false | Boolean | Expose count of immutable MemTables awaiting flush. |

---

## 9. History Server

The History Server serves information about completed jobs after the cluster has shut down.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `historyserver.archive.fs.dir` | (none) | String | Comma-separated list of directories the History Server monitors for job archives. Must match `jobmanager.archive.fs.dir`. |
| `historyserver.archive.fs.refresh-interval` | 10000 | Long | Interval (ms) to scan archive directories for new job archives. |
| `historyserver.web.address` | (none) | String | Address the History Server web UI binds to. |
| `historyserver.web.port` | 8082 | Integer | Port for the History Server web UI. |
| `historyserver.web.tmpdir` | (none) | String | Temporary directory for History Server web cache. |
| `historyserver.web.ssl.enabled` | false | Boolean | Enable SSL on the History Server REST interface. |
| `jobmanager.archive.fs.dir` | (none) | String | Directory where the JobManager writes job archives after completion. Should match `historyserver.archive.fs.dir`. |

---

## 10. Resource Orchestration

### YARN

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `yarn.application-attempt-failures-validity-interval` | 10000 | Long | Window (ms) for counting YARN application attempt failures. Failures outside this window are ignored. |
| `yarn.application.id` | (none) | String | YARN application ID — set when submitting to an existing YARN session. |
| `yarn.application.name` | "Flink session cluster" | String | Name shown in the YARN Resource Manager UI. |
| `yarn.application.type` | "Apache Flink" | String | YARN application type label. |
| `yarn.application.queue` | (none) | String | YARN queue to submit the application to. |
| `yarn.application.priority` | -1 | Integer | YARN application priority (-1 = default). |
| `yarn.containers.vcores` | -1 | Integer | Number of virtual cores per YARN container (-1 = use number of task slots). |
| `yarn.file-replication` | -1 | Integer | HDFS replication factor for YARN-submitted files (-1 = use HDFS default). |
| `yarn.flink-dist-jar` | (none) | String | Path to the Flink dist JAR on YARN. |
| `yarn.provided.lib.dirs` | (none) | String | Semicolon-separated list of pre-uploaded library directories on HDFS. Avoids re-uploading Flink JARs on each submission. |
| `yarn.ship-archives` | (none) | String | Comma-separated list of archive files to ship to YARN containers (auto-extracted). |
| `yarn.ship-files` | (none) | String | Comma-separated list of files/dirs to ship to YARN containers. |
| `yarn.staging-directory` | (none) | String | HDFS staging directory for YARN-submitted Flink resources. |
| `yarn.tags` | (none) | String | Comma-separated tags for the YARN application (shown in RM UI). |
| `yarn.taskmanager.node-label` | (none) | String | Node label expression for TaskManager containers (requires YARN node labels). |
| `yarn.heartbeat.interval` | 5 | Integer | Heartbeat interval (seconds) with YARN Resource Manager. |
| `yarn.maximum-failed-containers` | (none) | Integer | Max failed containers before the application exits. |
| `external-resource.<resource_name>.yarn.config-key` | (none) | String | YARN resource profile config key for a named external resource (e.g., GPU). |
| `flink.hadoop.<key>` | (none) | String | Pass Hadoop configuration properties with the `flink.hadoop.` prefix (e.g., `flink.hadoop.fs.s3a.access.key`). |
| `flink.yarn.<key>` | (none) | String | Pass YARN-specific configuration with the `flink.yarn.` prefix. |

### Kubernetes

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `kubernetes.cluster-id` | (none) | String | Unique identifier for the Flink cluster in Kubernetes. Used to name K8s resources (Services, Deployments, etc.). |
| `kubernetes.namespace` | "default" | String | Kubernetes namespace where Flink resources are created. |
| `kubernetes.service-account` | "default" | String | Service account used by Flink pods. Must have permissions to create/delete pods and services. |
| `kubernetes.container.image` | (none) | String | Docker image URI for Flink containers (e.g., `flink:1.19`). |
| `kubernetes.container.image.pull-policy` | "IfNotPresent" | Enum | Image pull policy: `Always`, `Never`, or `IfNotPresent`. |
| `kubernetes.container.image.pull-secrets` | (none) | List | Names of Kubernetes secrets for private registry authentication. |
| `kubernetes.rest.service.exposed.type` | "LoadBalancer" | String | How the REST service is exposed: `ClusterIP`, `NodePort`, or `LoadBalancer`. |
| `kubernetes.rest.service.port` | 8081 | Integer | Port exposed by the Kubernetes REST service. |
| `kubernetes.jobmanager.cpu` | 1.0 | Double | CPU request/limit for the JobManager pod. |
| `kubernetes.jobmanager.memory.limit-factor` | 1.0 | Double | Multiplier applied to JM memory for the pod limit (to accommodate JVM overhead). |
| `kubernetes.taskmanager.cpu` | -1.0 | Double | CPU request for TaskManager pods (-1 = derive from task slots). |
| `kubernetes.taskmanager.memory.limit-factor` | 1.0 | Double | Multiplier applied to TM memory for the pod memory limit. |
| `kubernetes.taskmanager.replicas` | 1 | Integer | Number of TaskManager replicas in session mode. |
| `kubernetes.jobmanager.replicas` | 1 | Integer | Number of standby JobManager replicas (for HA mode). |
| `kubernetes.pod-template-file` | (none) | String | Path to a custom Kubernetes Pod template YAML file (for advanced pod customization). |
| `kubernetes.pod-template-file.taskmanager` | (none) | String | TM-specific pod template, overrides the global pod template for TaskManagers. |
| `kubernetes.pod-template-file.jobmanager` | (none) | String | JM-specific pod template, overrides the global pod template for the JobManager. |
| `kubernetes.entry.path` | (none) | String | Entrypoint script path inside the Flink container. |
| `kubernetes.flink.conf.dir` | (none) | String | Path to Flink config directory inside the container. |
| `kubernetes.flink.log.dir` | (none) | String | Path to Flink log directory inside the container. |
| `kubernetes.hadoop.conf.config-map.name` | (none) | String | Name of an existing ConfigMap containing `core-site.xml` and `hdfs-site.xml`. Mounted into pods automatically. |
| `kubernetes.env.secretKeyRef` | (none) | List | List of environment variables sourced from Kubernetes Secrets (for injecting credentials). |
| `kubernetes.labels` | (none) | Map | Custom labels applied to all Flink Kubernetes resources. |
| `kubernetes.node-selector` | (none) | Map | Node selector labels to constrain which nodes Flink pods are scheduled on. |
| `kubernetes.tolerations` | (none) | List | Kubernetes tolerations for scheduling Flink pods on tainted nodes. |
| `kubernetes.hostnetwork.enabled` | false | Boolean | Enable host network mode for Flink pods. Useful in some bare-metal K8s setups. |
| `kubernetes.client.io-pool.size` | 4 | Integer | Thread pool size for Kubernetes client I/O. |
| `kubernetes.jobmanager.annotations` | (none) | Map | Annotations to apply to the JobManager pod. |
| `kubernetes.taskmanager.annotations` | (none) | Map | Annotations to apply to TaskManager pods. |
| `kubernetes.transactional-job-manager-service.enabled` | false | Boolean | Enable transactional JobManager service for safer leader election. |

---

## 11. Debugging & Expert Tuning

### Class Loading

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `classloader.resolve-order` | "child-first" | String | Class resolution order: `child-first` (user code takes precedence over Flink classpath — default, prevents library conflicts) or `parent-first` (Flink classpath takes precedence). |
| `classloader.parent-first-class-loading.packages` | (flink internals) | List | Packages always loaded parent-first regardless of `resolve-order`. Add packages here if a library must share state with Flink internals. |
| `classloader.fail-on-metaspace-oom-error` | true | Boolean | Kill the TM on `OutOfMemoryError: Metaspace` rather than leaving it in a broken state. |
| `classloader.check-leaked-classloader` | true | Boolean | Warn when user classloaders are not properly closed after job completion. |

### Advanced State Backend (RocksDB) Options

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `state.backend.rocksdb.memory.managed` | true | Boolean | Let Flink's managed memory control RocksDB memory (recommended). Disable only for manual RocksDB tuning. |
| `state.backend.rocksdb.memory.fixed-per-slot` | (none) | MemorySize | Fixed RocksDB memory budget per slot. Overrides managed memory when set. |
| `state.backend.rocksdb.memory.write-buffer-ratio` | 0.5 | Double | Fraction of RocksDB memory budget used for write buffers. |
| `state.backend.rocksdb.memory.high-prio-pool-ratio` | 0.1 | Double | Fraction reserved as high-priority block cache (index/filter blocks). |
| `state.backend.rocksdb.block.blocksize` | 4kb | MemorySize | RocksDB data block size. Larger = better compression, smaller = faster point lookups. |
| `state.backend.rocksdb.block.cache-size` | 8mb | MemorySize | Block cache size per column family when not using managed memory. |
| `state.backend.rocksdb.compaction.level.max-size-level-base` | 256mb | MemorySize | Max size of Level-1 in leveled compaction. Controls write amplification. |
| `state.backend.rocksdb.compaction.level.target-file-size-base` | 64mb | MemorySize | Target SST file size for compaction. |
| `state.backend.rocksdb.compaction.style` | LEVEL | Enum | Compaction strategy: `LEVEL` (good general use), `UNIVERSAL` (better for large state with infrequent reads), `FIFO` (for small, time-series-like data). |
| `state.backend.rocksdb.thread.num` | 1 | Integer | Number of RocksDB background threads for compaction and flush. Increase for write-heavy workloads. |
| `state.backend.rocksdb.writebuffer.count` | 2 | Integer | Number of write buffers (MemTables) per column family. More buffers = fewer stalls during flush. |
| `state.backend.rocksdb.writebuffer.size` | 64mb | MemorySize | Size of each write buffer. Larger = fewer flushes, more memory. |
| `state.backend.rocksdb.files.open` | -1 | Integer | Max open SST files per RocksDB instance (-1 = unlimited). Limit if hitting OS file descriptor limits. |
| `state.backend.rocksdb.checkpoint.transfer.thread.num` | 4 | Integer | Number of threads used during checkpoint upload/download. Increase for faster checkpointing. |
| `state.backend.rocksdb.use-bloom-filter` | false | Boolean | Enable bloom filters on SST files to speed up point lookups. Recommended for read-heavy state access. |
| `state.backend.rocksdb.predefined-options` | DEFAULT | Enum | Apply a predefined RocksDB option profile: `DEFAULT`, `SPINNING_DISK_OPTIMIZED`, `SPINNING_DISK_OPTIMIZED_HIGH_MEM`, or `FLASH_SSD_OPTIMIZED`. |

### Advanced Cluster Options

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `cluster.io-pool.size` | (none) | Integer | Thread pool size for cluster-level I/O operations. Defaults to number of CPU cores. |
| `cluster.registration.initial-timeout` | 100ms | Duration | Initial timeout for cluster component registration. Doubles on failure up to max. |
| `cluster.registration.max-timeout` | 30s | Duration | Maximum registration timeout. |
| `cluster.registration.refused-registration-delay` | 30s | Duration | Pause after a registration is explicitly refused before retrying. |
| `cluster.registration.error-registration-delay` | 10s | Duration | Pause after a registration error before retrying. |
| `cluster.services.shutdown-timeout` | 30s | Duration | Timeout for graceful shutdown of cluster services. |

### Advanced JobManager Options

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `jobmanager.execution.failover-strategy` | region | String | Failover strategy: `full` (restart entire job on any failure) or `region` (restart only the connected region — more efficient). |
| `jobmanager.execution.attempt-history-size` | 16 | Integer | Number of execution attempt history entries kept per vertex for debugging. |
| `jobmanager.slot.idle.timeout` | 50000 | Long | Time (ms) an idle slot is kept before being released. Lower = faster resource reclaim, higher = fewer allocation requests. |
| `jobmanager.adaptive-scheduler.resource-wait-timeout` | 5min | Duration | How long the adaptive scheduler waits to acquire required resources before failing. |
| `jobmanager.adaptive-scheduler.stabilization-timeout` | 10s | Duration | Time to wait for cluster to stabilize after scaling before rescheduling. |

### Advanced TaskManager Options

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `taskmanager.numberOfTaskSlots` | 1 | Integer | Number of parallel task slots per TaskManager. Rule of thumb: one slot per CPU core. |
| `taskmanager.slot.timeout` | 10000 | Long | Timeout (ms) for TaskManager slot requests. |
| `taskmanager.debug.memory.log` | false | Boolean | Periodically log TaskManager memory usage for debugging OOM issues. |
| `taskmanager.debug.memory.log-interval` | 5000 | Long | Interval (ms) between memory usage log entries when debug logging is enabled. |
| `taskmanager.network.blocking-shuffle.compression.enabled` | false | Boolean | Enable compression for blocking shuffle data (used in batch jobs). |
| `taskmanager.network.compression.codec` | "LZ4" | String | Compression codec for network data: `LZ4`, `LZO`, or `ZSTD`. |
| `taskmanager.network.detailed-metrics` | false | Boolean | Expose detailed per-channel network metrics. High overhead — enable only for debugging. |

---

## 12. JVM & Logging

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `env.java.home` | (none) | String | Override the Java installation used by Flink. Useful when multiple JDKs are installed. |
| `env.java.opts` | (none) | String | JVM options applied to all Flink JVM processes (e.g., `-XX:+UseG1GC`). |
| `env.java.opts.jobmanager` | (none) | String | JVM options applied only to the JobManager process. |
| `env.java.opts.taskmanager` | (none) | String | JVM options applied only to TaskManager processes. |
| `env.java.opts.historyserver` | (none) | String | JVM options for the History Server process. |
| `env.java.opts.client` | (none) | String | JVM options for the Flink client process. |
| `env.log.dir` | `<FLINK_HOME>/log` | String | Directory where Flink writes log files. |
| `env.log.max` | 5 | Integer | Maximum number of old log files to retain (rolling). |
| `env.log.level` | "INFO" | String | Root log level: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`. |
| `env.stdout-redirect.enabled` | false | Boolean | Redirect stdout/stderr to log files. |
| `env.ssh.opts` | (none) | String | Additional SSH options for the `start-cluster.sh` script. |
| `env.pid.dir` | `<FLINK_HOME>/tmp` | String | Directory for PID files created by Flink start scripts. |

---

## 13. Experimental

> These features are not production-ready. Use in dev/test only.

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `deployment.profiling.enabled` | false | Boolean | Enable JVM profiling of Flink processes. Useful for performance analysis. |
| `flink.pluggable-state-store.enabled` | false | Boolean | Enable experimental pluggable state store backends. |
| `taskmanager.adaptive-batch-scheduler.enabled` | false | Boolean | Enable the adaptive batch scheduler which auto-tunes task parallelism based on data volume. |
| `taskmanager.adaptive-batch-scheduler.min-parallelism` | 1 | Integer | Minimum parallelism for the adaptive batch scheduler. |
| `taskmanager.adaptive-batch-scheduler.max-parallelism` | 128 | Integer | Maximum parallelism cap for the adaptive batch scheduler. |

---

## 14. Miscellaneous

| Key | Default | Type | Description |
|-----|---------|------|-------------|
| `fs.default-scheme` | (none) | String | Default file system scheme applied to paths without an explicit scheme (e.g., setting `file` treats all bare paths as local). |
| `fs.allowed-fallback-filesystems` | (none) | String | Semicolon-separated URI schemes for which Flink falls back to Hadoop's `FileSystem` implementation. |
| `io.tmp.dirs` | varies | String | Comma/semicolon-separated directories for Flink temporary files (spill files, shuffle data). Use fast local SSDs here. |
| `sink.committer.retries` | 10 | Integer | Number of retries for committable sink operations on retriable errors (e.g., transient network failures). |
| `parallelism.default` | 1 | Integer | Default job parallelism when not set programmatically. Always override this in production. |
| `pipeline.auto-watermark-interval` | 200ms | Duration | Interval at which watermarks are emitted automatically in event-time processing. |
| `pipeline.max-parallelism` | -1 | Integer | Maximum parallelism for the job (used to determine key group count). Must be >= actual parallelism. Once set, cannot be changed without a fresh savepoint. |
| `pipeline.operator-chaining` | true | Boolean | Enable operator chaining (fusing adjacent operators in the same thread). Disable only for debugging or profiling. |
| `pipeline.object-reuse` | false | Boolean | Reuse objects between operator invocations for performance. **Unsafe** if your operators retain references to input objects. |

---

## 15. Deprecated Options

> These keys still work in Flink 1.19 for backward compatibility but will be removed in a future version. Migrate to the replacements shown.

| Deprecated Key | Replacement | Notes |
|----------------|-------------|-------|
| `flink-conf.yaml` (file) | `config.yaml` | The config file name itself changed in 1.19. |
| `execution.buffer-timeout` | `pipeline.output-flush-interval` | Renamed for clarity. |
| `taskmanager.memory.size` | `taskmanager.memory.task.heap.size` | Part of the unified memory model introduced in 1.10. |
| `jobmanager.heap.size` | `jobmanager.memory.process.size` | Same — unified memory model. |
| `heartbeat.interval` | `heartbeat.interval` (per component) | Replaced by per-component heartbeat configs. |
| `state.backend` | `state.backend.type` | Key renamed. |
| `recovery.mode` | `high-availability.type` | Key renamed. |
| `ha.zookeeper.quorum` | `high-availability.zookeeper.quorum` | Prefix changed from `ha.` to `high-availability.`. |
| `ha.storageDir` | `high-availability.storageDir` | Same prefix change. |
| `yarn.heap-size` | `jobmanager.memory.process.size` / `taskmanager.memory.process.size` | Replaced by unified memory model. |

---

## Quick Reference — Most Common Production Settings

```yaml
# config.yaml

# Parallelism
parallelism.default: 4
pipeline.max-parallelism: 128

# JobManager
jobmanager.memory.process.size: 1600m
jobmanager.rpc.address: jobmanager
jobmanager.rpc.port: 6123

# TaskManager
taskmanager.memory.process.size: 4096m
taskmanager.numberOfTaskSlots: 4

# Checkpointing
state.backend.type: rocksdb
state.backend.incremental: true
state.checkpoints.dir: s3://my-bucket/flink/checkpoints
state.checkpoints.num-retained: 3
execution.checkpointing.interval: 60s
execution.checkpointing.mode: EXACTLY_ONCE
execution.checkpointing.timeout: 5min

# Restart Strategy
restart-strategy.type: exponential-delay
restart-strategy.exponential-delay.initial-backoff: 1s
restart-strategy.exponential-delay.max-backoff: 1min
restart-strategy.exponential-delay.backoff-multiplier: 2.0

# Web UI
rest.port: 8081
```
