**Materialized Views: Concepts and Applications in Databases and Flink**

---

### 1. Materialized Views: What Are They and Where Were They Introduced?

**Materialized Views (MVs)** were first introduced in traditional **Relational Database Management Systems (RDBMS)** like **PostgreSQL**, **Oracle**, and **SQL Server** to improve performance by caching the result of expensive queries.

> **Analogy:** Imagine you repeatedly solve a math problem every time someone asks — time-consuming, right? Now think of a sticky note where you write the answer once and just show it whenever needed — that's a materialized view.

---

### 2. Materialized Views in Traditional Databases (e.g., PostgreSQL)

In **PostgreSQL**, an MV stores the result of a query physically and requires manual or scheduled refreshing.

**Example:**

```sql
CREATE MATERIALIZED VIEW user_summary AS
SELECT user_id, COUNT(*) AS total_actions, AVG(score) as avg_score
FROM user_logs
GROUP BY user_id;

REFRESH MATERIALIZED VIEW user_summary;
```

**Aggregations Commonly Used in MVs:**

* `COUNT()` – Count of rows
* `SUM()` – Total of numeric column
* `AVG()` – Average of numeric column
* `MIN()` / `MAX()` – Extremes within groups
* `GROUP BY` – Breakdowns per category
* `JOIN` – Combine related tables for richer output

**Use Cases:**

* Business Intelligence dashboards
* Performance-optimized reporting
* Precomputed analytics for fast query response

---

### 3. Materialized Views in Apache Flink (Streaming Context)

In Flink, materialized views aren't stored as physical tables but rather as **continuously updated state** in memory or RocksDB (a local embedded key-value store).

> **Analogy:** Instead of saving the result once and refreshing it later, imagine someone updating the sticky note every second as new data comes in — live tracking.

Flink updates this state **in real-time** based on incoming stream events and then emits updated results to a sink.

---

### 4. What Counts as a Materialized View in Flink?

Any transformation or aggregation that maintains up-to-date state counts as a materialized view:

| Operation  | Description                  | Example Use Case                      |
| ---------- | ---------------------------- | ------------------------------------- |
| `COUNT(*)` | Count of all records per key | Clicks per user                       |
| `SUM(col)` | Sum of a field               | Total purchase amount per category    |
| `AVG(col)` | Average of values            | Avg. session time per user            |
| `MIN(col)` | Minimum value                | Min temperature per device            |
| `MAX(col)` | Maximum value                | Peak CPU usage per server             |
| `GROUP BY` | Group-level computation      | Aggregates by region, product, etc.   |
| `JOIN`     | Real-time lookup joins       | Enrich user events with profile info  |
| `WINDOW`   | Time-based aggregation       | Daily active users in sliding windows |

These are all **maintained incrementally** as data flows in, avoiding full recomputation.

---

### 5. Flink SQL: Sample Source, Sink, and Insert Queries

#### ✅ Source Table (Kafka)

```sql
CREATE TABLE user_actions (
  user_id STRING,
  action STRING,
  score DOUBLE,
  event_time TIMESTAMP(3),
  WATERMARK FOR event_time AS event_time - INTERVAL '5' SECOND
) WITH (
  'connector' = 'kafka',
  'topic' = 'user_actions',
  'properties.bootstrap.servers' = 'kafka:9092',
  'format' = 'json'
);
```

#### ✅ Sink Table (PostgreSQL)

```sql
CREATE TABLE user_aggregates (
  user_id STRING,
  action_count BIGINT,
  avg_score DOUBLE,
  PRIMARY KEY (user_id) NOT ENFORCED
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:postgresql://postgres:5432/mydb',
  'table-name' = 'user_aggregates',
  'username' = 'myuser',
  'password' = 'mypassword'
);
```

#### ✅ Insert Query – Streaming Materialized View

```sql
INSERT INTO user_aggregates
SELECT
  user_id,
  COUNT(*) AS action_count,
  AVG(score) AS avg_score
FROM user_actions
GROUP BY user_id;
```

This creates a **live materialized view** continuously reflecting user activity.

---

### 6. Use Cases for Flink Materialized Views

* **Real-Time Metrics** – Application or user stats
* **IoT Monitoring** – Sensor status, average readings
* **Fraud Detection** – Count of failed logins per user
* **Operational Dashboards** – Live traffic or sales updates
* **Gaming** – Leaderboards updated with every action

---

### 7. Output Sinks for Flink MVs

Flink can write materialized views to various sinks:

| Sink Type     | Description                            |
| ------------- | -------------------------------------- |
| PostgreSQL    | RDBMS for structured aggregates        |
| ClickHouse    | OLAP store for real-time analytics     |
| Apache Doris  | MPP OLAP, optimized for streaming      |
| StarRocks     | OLAP system for real-time ingestion    |
| Apache Paimon | Unified stream-batch lakehouse storage |

---

### 8. Comparison: PostgreSQL vs Flink Materialized Views

| Feature          | PostgreSQL MV          | Flink MV                           |
| ---------------- | ---------------------- | ---------------------------------- |
| Update Frequency | Manual/Scheduled       | Automatic (per event)              |
| Storage          | Physical table         | In-memory or RocksDB state         |
| Latency          | Delayed (batch)        | Real-time (low-latency)            |
| Reusability      | Requery by clients     | Needs sink output to be reused     |
| Query Target     | SQL clients (Postgres) | Downstream sinks (OLAP, DBs, etc.) |

---

### 9. Visualization and Analytics

After emitting materialized view data to sinks like ClickHouse or Doris, you can connect these to BI tools:

* **Grafana**
* **Apache Superset**
* **Metabase**

These allow dashboards over real-time materialized views.

---

### 10. Key Takeaways

* Materialized Views avoid repeated computation by maintaining precomputed results.
* PostgreSQL stores these as static, refreshable tables.
* Flink manages them as real-time, event-driven state with continuous updates.
* MVs in Flink power real-time analytics across user activity, metrics, IoT, and more.
* Sinks like ClickHouse, PostgreSQL, and Doris persist these views for querying and dashboards.

---
