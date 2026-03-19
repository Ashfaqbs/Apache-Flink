# InventoryReorderJob — Automated Inventory Reorder Recommendations (Multi-Agent Workflow)

## What It Does

A two-stage AI pipeline for real-time inventory management.

**Stage 1 — Per-sale velocity classification** (`SalesVelocityAgent`): reads each sale event from
Kafka topic `sale-events` and classifies sales velocity as **HIGH / MEDIUM / LOW** with an estimated
days-to-stockout and a list of specific stock concerns (`SkuVelocityResult`).

**Windowing**: a 5-minute tumbling window keys by `skuId` and aggregates all velocity results seen
in that window into a `SkuWindowSummary` — averaging days-to-stockout estimates, counting sale
events as an activity proxy, tracking the minimum stock level seen, and unioning all stock concerns.

**Stage 2 — Reorder recommendation** (`ReorderAgent`): receives the `SkuWindowSummary` and
generates a reorder recommendation with **CRITICAL / HIGH / MEDIUM / LOW** urgency and a specific
suggested order quantity. For CRITICAL or HIGH urgency the agent calls two tools autonomously:

- `notifyProcurement(skuId, productName, urgency, suggestedQty)` — fires a purchase order request
  to the ERP / Procurement API.
- `sendRestockAlert(warehouseId, skuId, message)` — sends an immediate alert via SMS / WMS webhook
  to warehouse operations.

The final `ReorderRecommendation` is sinked to Kafka topic `reorder-recommendations`.

---

## What Problem It Solves

Static reorder point (ROP) systems set thresholds once and never adapt. A SKU that normally sells
10 units per day but suddenly sells 80 units per day — flash sale, viral moment, seasonal spike —
will not trigger a reorder until it crosses the static threshold. By that time it is already out of
stock, and the static system has no way to distinguish a momentary blip from a sustained velocity
shift.

This pipeline detects velocity acceleration in real time per SKU per warehouse. The tumbling window
aggregates the signal across multiple sale events so that a single anomalous transaction does not
trigger a false positive, while a genuine surge across several events within the window will.
Agent 2 synthesizes the aggregated signal into an urgency-ranked recommendation with a
business-context-aware suggested order quantity — not just a binary reorder flag. The LLM can
weigh current stock level, average days-to-stockout, and the pattern of concerns together and
recommend "order 500 units urgently" versus "order 50 units as a routine replenishment".

---

## Flow Diagram

```
[Kafka Source]
  Topic: sale-events
  Schema: { skuId, productName, quantitySold,
            currentStock, warehouseId, timestamp }
        |
        v
+------------------------------------------+
|  Agent 1: SalesVelocityAgent              |
|  (Workflow — Groq)                        |
|                                           |
|  Per sale event:                          |
|  - Classify velocity: HIGH/MEDIUM/LOW     |
|  - Estimate days to stockout              |
|  - Identify stock concerns:               |
|    below_safety_stock /                   |
|    high_sell_through_rate /              |
|    accelerating_depletion               |
|                                           |
|  Output: SkuVelocityResult               |
|  { skuId, productName, salesVelocity,    |
|    estimatedDaysToStockout, stockConcerns }|
+------------------------------------------+
        |
        v keyed by skuId
+------------------------------------------+
|  Tumbling Window — 5 minutes             |
|                                           |
|  Per SKU per window:                     |
|  - Count sale events (activity proxy)    |
|  - Average days-to-stockout estimates    |
|  - Track minimum stock level seen        |
|  - Union all stock concerns              |
|                                           |
|  Output: SkuWindowSummary JSON           |
+------------------------------------------+
        |
        v
+------------------------------------------+
|  Agent 2: ReorderAgent                    |
|  (Workflow — Groq)                        |
|                                           |
|  - Assign urgency: CRITICAL/HIGH/         |
|    MEDIUM/LOW                             |
|  - Suggest order quantity                 |
|  - Generate recommendations list         |
|                                           |
|  If CRITICAL or HIGH:                    |
|  -> notifyProcurement(skuId, name,       |-----> [ERP / Procurement API]
|       urgency, qty)                      |
|  -> sendRestockAlert(warehouseId,        |-----> [SMS / WMS Webhook]
|       skuId, message)                    |
|                                           |
|  Output: ReorderRecommendation           |
|  { skuId, productName, warehouseId,      |
|    urgency, suggestedOrderQuantity,      |
|    recommendations[] }                   |
+------------------------------------------+
        |
        v
[Kafka Sink]
  Topic: reorder-recommendations
        |
        v
  +----------+----------+----------+
  |          |          |          |
  v          v          v          v
Procurement Warehouse  ERP/WMS   Inventory
Dashboard   Alerts     System    Analytics
```

---

## Urgency Decision Guide

| `avgDaysToStockout` | Typical Urgency |
|---------------------|-----------------|
| 0 – 2 days | CRITICAL |
| 3 – 5 days | HIGH |
| 6 – 14 days | MEDIUM |
| 14+ days | LOW |

The LLM may override the days-based default when specific concern combinations warrant it. For
example, 8 days to stockout paired with `below_safety_stock` + `accelerating_depletion`
simultaneously may push the agent to HIGH rather than MEDIUM because the velocity trend makes the
8-day estimate optimistic.

---

## Key Classes

| Class | Package | Role |
|-------|---------|------|
| `InventoryReorderJob` | `retail.job` | Main entry point — wires Kafka source, both agents, window, Kafka sink |
| `SalesVelocityAgent` | `retail.agent` | Stage 1 agent — classifies velocity and estimates days-to-stockout per sale event |
| `ReorderAgent` | `retail.agent` | Stage 2 agent — assigns urgency, suggests order quantity; calls procurement and restock tools if CRITICAL or HIGH |
| `SaleEvent` | `retail.dto` | Input DTO deserialized from Kafka |
| `SkuVelocityResult` | `retail.dto` | Intermediate DTO — per-event velocity classification from Agent 1 |
| `SkuWindowSummary` | `retail.dto` | Aggregated window result passed to Agent 2 |
| `ReorderRecommendation` | `retail.dto` | Final output DTO serialized to Kafka |
| `SaleEventDeserializationSchema` | `retail.job` | JSON → SaleEvent |
| `ReorderRecommendationSerializer` | `retail.job` | ReorderRecommendation → JSON bytes |

---

## Kafka Topics

| Topic | Direction | Schema |
|-------|-----------|--------|
| `sale-events` | Source | `{ skuId, productName, quantitySold, currentStock, warehouseId, timestamp }` |
| `reorder-recommendations` | Sink | `{ skuId, productName, warehouseId, urgency, suggestedOrderQuantity, recommendations }` |

---

## How to Run

1. **Start infrastructure** (from the `docker/` directory):
   ```bash
   docker-compose up -d
   ```

2. **Set the Groq API key**:
   ```bash
   export GROQ_API_KEY=gsk_your_key_here
   ```

3. **Build the application** (from the `flink-ai-app/` directory):
   ```bash
   mvn clean package -DskipTests
   ```

4. **Produce test sale events** (from the repo root):
   ```bash
   python python/produce_sale_events.py
   ```

5. **Submit the job** — open the Flink UI at `http://localhost:8081`, upload the jar, and set the
   main class to:
   ```
   com.example.ai.retail.job.InventoryReorderJob
   ```

6. **Monitor output** — consume from the `reorder-recommendations` topic:
   ```bash
   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
     --topic reorder-recommendations --from-beginning
   ```

> **Window timing note**: results only appear after the 5-minute tumbling window closes. For local
> development and demos, reduce the window duration to 30 seconds by changing `Duration.ofMinutes(5)`
> to `Duration.ofSeconds(30)` in `InventoryReorderJob.java`.

---

## Sample Input / Output

### Input — critical stockout scenario arriving on `sale-events`

A high-velocity SKU with only 45 units remaining in stock and 80 units sold in a single event —
the sell-through rate exceeds current stock:

```json
{
  "skuId": "SKU-00412",
  "productName": "Wireless Noise-Cancelling Headphones",
  "quantitySold": 80,
  "currentStock": 45,
  "warehouseId": "WH-EAST-03",
  "timestamp": "2026-03-16T14:05:00Z"
}
```

The `quantitySold` (80) already exceeds `currentStock` (45), meaning the warehouse is effectively
out of stock on this event alone. Agent 1 will classify velocity as HIGH, estimate days-to-stockout
at 0, and flag `high_sell_through_rate` + `accelerating_depletion`. After the window closes,
Agent 2 receives this summary and assigns CRITICAL urgency.

### Output — recommendation emitted to `reorder-recommendations`

```json
{
  "skuId": "SKU-00412",
  "productName": "Wireless Noise-Cancelling Headphones",
  "warehouseId": "WH-EAST-03",
  "urgency": "CRITICAL",
  "suggestedOrderQuantity": 500,
  "recommendations": [
    "Current stock (45 units) is fully consumed by in-flight sales demand (80 units sold)",
    "Estimated days to stockout: 0 — warehouse is effectively depleted",
    "High sell-through rate indicates sustained demand, not a one-off spike",
    "Recommend emergency procurement of 500 units to cover 6-day demand buffer at current velocity",
    "Procurement team notified and warehouse restock alert dispatched to WH-EAST-03"
  ]
}
```

Agent 2 identified a zero-day stockout scenario, assigned CRITICAL urgency, called
`notifyProcurement` (triggering a purchase order in the ERP system) and `sendRestockAlert`
(dispatching an SMS alert to warehouse operations at WH-EAST-03) autonomously, and emitted the
`ReorderRecommendation` — all within the same Flink operator invocation after the window closed.
