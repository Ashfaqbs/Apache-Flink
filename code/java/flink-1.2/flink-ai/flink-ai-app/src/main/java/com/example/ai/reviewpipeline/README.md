# Review Pipeline Job

## What it does

Reads product reviews from a file, runs them through two chained LLM agents (via Groq),
and publishes actionable product improvement suggestions to a Kafka topic.

- **Agent 1** scores each review (1-5 stars) and extracts reasons for dissatisfaction.
  It also has a tool: if the review explicitly mentions a shipping or delivery problem,
  it calls `notifyShippingManager` before producing its structured output.
- A **1-minute tumbling window** then groups all analysis results per product, builds a
  score histogram (% of 1-star through 5-star reviews), and collects every dissatisfaction
  reason seen in that window.
- **Agent 2** receives that aggregated window summary and generates exactly 3 actionable
  product improvement suggestions based on the score distribution and the collected reasons.
- The final suggestions land in the `product-suggestions` Kafka topic, ready for downstream
  consumers such as dashboards, product teams, or alerting pipelines.

---

## What problem it solves

Processing product reviews manually at scale does not work. A product with thousands of
reviews needs automated summarization to surface recurring complaints and translate them
into concrete action items — without a human reading every line.

This pipeline solves that by treating the review stream as a continuous data source:
reviews flow in, the first LLM distills each one into a structured signal (score + reasons),
the window layer aggregates those signals into a product-level picture, and the second LLM
turns that picture into decision-ready suggestions — all in near real-time.

The two-agent design separates concerns cleanly:
- Agent 1 is fast and per-event (one LLM call per review).
- Agent 2 is deliberate and per-product (one LLM call per windowed batch).
This means the expensive "synthesis" step only runs once per window, not once per review.

---

## Flow

```
+-------------------------+
|   input_data.txt        |
|   (product reviews,     |
|    one JSON per line)   |
+----------+--------------+
           |
           | Flink filesystem connector
           | (Table API — typed rows: id, review)
           v
+----------+--------------+
|  Agent 1                |
|  ReviewAnalysisAgent    |
|                         |
|  LLM: Groq              |
|  Model: llama-3.3-70b   |
|                         |
|  Input:  id + review    |
|  Output: score (1-5)    |
|          + reasons[]    |
|                         |
|  Tool: notifyShipping   | -----> [Shipping team notification]
|        Manager()        |        (stdout / webhook / Slack)
|        (if shipping     |
|         complaint)      |
+----------+--------------+
           |
           | ReviewAnalysisResult stream
           | keyed by product id
           v
+----------+--------------+
|  Tumbling Window        |
|  Duration: 1 minute     |
|  Key:      product id   |
|                         |
|  Aggregates per window: |
|  - score histogram      |
|    [x% 1-star,          |
|     x% 2-star, ...]     |
|  - all reasons[]        |
|    collected            |
+----------+--------------+
           |
           | ReviewWindowSummary (JSON)
           v
+----------+--------------+
|  Agent 2                |
|  ProductSuggestion      |
|  Agent                  |
|                         |
|  LLM: Groq              |
|  Model: llama-3.3-70b   |
|                         |
|  Input:  product id     |
|          + histogram    |
|          + reasons[]    |
|  Output: 3 actionable   |
|          suggestions    |
+----------+--------------+
           |
           | ProductSuggestion (JSON)
           v
+----------+--------------+
|  Kafka Sink             |
|  Topic:                 |
|  product-suggestions    |
+----------+--------------+
           |
           v
  +--------+--------+--------+
  |        |        |        |
  v        v        v        v
Dash-   Product  Alert   Any other
board   team     system  consumer
```

---

## Key classes

| Class | Role |
|-------|------|
| `ReviewAnalysisAgent` | Agent 1 — scores reviews, detects shipping issues via tool |
| `ProductSuggestionAgent` | Agent 2 — generates improvement suggestions from window data |
| `ReviewPipelineJob` | Main entry point — wires the full pipeline together |
| `ReviewAnalysisResult` | DTO: Agent 1 output (id, score, reasons) |
| `ReviewWindowSummary` | DTO: window aggregation output (id, histogram, reasons) |
| `ProductSuggestion` | DTO: Agent 2 output — what gets sinked to Kafka |
| `ProductSuggestionSerializer` | Serializes ProductSuggestion to JSON bytes for Kafka |

---

## How to run

**1. Set your Groq API key**
```bash
export GROQ_API_KEY=your_key_here
```

**2. Start the infrastructure**
```bash
cd docker/
docker-compose up -d
```

**3. Build the fat jar**
```bash
cd flink-ai-app/
mvn clean package -q
```

**4. Submit to Flink**

Open http://localhost:8081, go to Submit New Job, upload the shaded jar from
`target/flink-ai-app-1.0-SNAPSHOT.jar`, and set the entry class to:

```
com.example.ai.reviewpipeline.job.ReviewPipelineJob
```

**5. Watch the output topic**
```bash
# from inside the kafka container
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic product-suggestions \
  --from-beginning
```

---

## Configuration

| Parameter | Value | Where to change |
|-----------|-------|-----------------|
| LLM provider | Groq (OpenAI-compatible) | `ReviewAnalysisAgent`, `ProductSuggestionAgent` |
| Model | `llama-3.3-70b-versatile` | Both agent `@ChatModelSetup` methods |
| Window size | 1 minute (tumbling) | `ReviewPipelineJob` — `TumblingProcessingTimeWindows.of(...)` |
| Async LLM threads | 2 | `ReviewPipelineJob` — `AgentExecutionOptions.NUM_ASYNC_THREADS` |
| Kafka bootstrap | `kafka:9093` | `ReviewPipelineJob` — `KafkaSink` builder |
| Output topic | `product-suggestions` | `ReviewPipelineJob` — `KafkaSink` builder |
| API key | env var `GROQ_API_KEY` | `docker-compose.yml` + shell env |
