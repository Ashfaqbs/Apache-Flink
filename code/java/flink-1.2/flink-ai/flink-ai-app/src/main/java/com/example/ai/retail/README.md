# Retail AI Pipelines

Two real-time AI-powered Flink pipelines for retail use cases, built with Flink Agents and Groq LLM.

---

## ChurnDetectionJob

### What It Does
Analyzes customer behavioral events in real time to classify churn risk as LOW, MEDIUM, HIGH, or CRITICAL. For HIGH and CRITICAL risk customers, the LLM autonomously triggers a retention offer, updates the CRM system, and optionally escalates the case to a human retention agent.

### Problem It Solves
Customer churn is expensive — acquiring a new customer costs 5-7x more than retaining one. Traditional churn models run in batch (daily/weekly), meaning intervention happens too late. This pipeline detects churn signals as they occur (inactivity events, support tickets, cart abandonment) and triggers retention actions within seconds of the signal.

### Flow

```
[Kafka: customer-events]
        |
        | JSON: { customerId, eventType, sessionCount,
        |         daysSinceLastPurchase, totalSpend, timestamp }
        v
[ChurnDetectionAgent — Groq LLM (ReAct)]
        |
        |  Step 1: Assess churn risk (LOW/MEDIUM/HIGH/CRITICAL)
        |  Step 2: Identify churn signals
        |  Step 3 (if HIGH/CRITICAL):
        |    -> triggerRetentionOffer(customerId, offerType)   [Tool]
        |    -> notifyCRMSystem(customerId, churnRisk, signals)[Tool]
        |    -> escalateToHumanAgent(customerId, reason)       [Tool, if needed]
        |  Step 4: Emit structured JSON response
        v
[ChurnAlert: { customerId, churnRisk, churnSignals,
               actionTaken, timestamp }]
        |
        v
[Kafka: churn-alerts]
        |
        v
[Downstream: CRM system, retention dashboard, marketing automation]
```

### Key Classes
| Class | Role |
|-------|------|
| `ChurnDetectionAgent` | ReAct LLM agent — reasons and calls retention tools |
| `CustomerEvent` | Input DTO from Kafka |
| `ChurnAlert` | Output DTO to Kafka |
| `CustomerEventDeserializationSchema` | Kafka JSON deserializer |
| `ChurnAlertSerializer` | Kafka JSON serializer |
| `ChurnDetectionJob` | Main entry point |

### Kafka Topics
| Topic | Direction | Description |
|-------|-----------|-------------|
| `customer-events` | Source | Behavioral events from the retail platform |
| `churn-alerts` | Sink | AI-generated churn risk assessments |

### How to Run
```bash
export GROQ_API_KEY=gsk_your_key_here

mvn clean package -DskipTests

flink run -c com.example.ai.retail.job.ChurnDetectionJob \
    target/flink-ai-app-*.jar
```

---

## InventoryReorderJob

### What It Does
A two-stage AI pipeline that processes sale events from Kafka. Agent 1 assesses the sales velocity and estimated days to stockout for each sale event. A 5-minute tumbling window aggregates velocity signals per SKU. Agent 2 generates an urgency-ranked reorder recommendation with a suggested order quantity. For CRITICAL and HIGH urgency, procurement and warehouse teams are automatically notified via tools.

### Problem It Solves
Stockouts cost retailers an average of 4% of annual revenue. Traditional reorder point systems use static thresholds that cannot adapt to real-time velocity changes (e.g., a product going viral, seasonal spikes). This pipeline detects accelerating depletion in real time and initiates the reorder process automatically — before stockout occurs.

### Flow

```
[Kafka: sale-events]
        |
        | JSON: { skuId, productName, quantitySold,
        |         currentStock, warehouseId, timestamp }
        v
[Agent 1: SalesVelocityAgent — Groq LLM (Workflow)]
        |
        |  - Classifies velocity: HIGH / MEDIUM / LOW
        |  - Estimates days to stockout
        |  - Identifies stock concerns
        |  - Emits: SkuVelocityResult { skuId, productName, salesVelocity,
        |           estimatedDaysToStockout, stockConcerns }
        v
[Tumbling Window: 5 minutes, keyed by skuId]
        |
        |  - Counts sale events (activity proxy)
        |  - Averages days-to-stockout estimates
        |  - Unions all stock concerns
        |  - Outputs: SkuWindowSummary JSON
        v
[Agent 2: ReorderAgent — Groq LLM (Workflow)]
        |
        |  - Assigns urgency: CRITICAL / HIGH / MEDIUM / LOW
        |  - Suggests order quantity
        |  - If CRITICAL or HIGH:
        |      -> notifyProcurement(skuId, productName, urgency, qty)  [Tool]
        |      -> sendRestockAlert(warehouseId, skuId, message)        [Tool]
        |  - Emits: ReorderRecommendation { skuId, productName, warehouseId,
        |           urgency, suggestedOrderQuantity, recommendations }
        v
[Kafka: reorder-recommendations]
        |
        v
[Downstream: ERP/WMS system, procurement dashboard, warehouse alerts]
```

### Key Classes
| Class | Role |
|-------|------|
| `SalesVelocityAgent` | Agent 1 — per-event velocity assessment |
| `ReorderAgent` | Agent 2 — reorder recommendation with tools |
| `SaleEvent` | Input DTO |
| `SkuVelocityResult` | Agent 1 output, window key type |
| `SkuWindowSummary` | Aggregated window data for Agent 2 |
| `ReorderRecommendation` | Final output DTO |
| `InventoryReorderJob` | Main entry point with window function |

### Kafka Topics
| Topic | Direction | Description |
|-------|-----------|-------------|
| `sale-events` | Source | Sale transactions from POS/e-commerce |
| `reorder-recommendations` | Sink | AI-generated restock recommendations |

### How to Run
```bash
export GROQ_API_KEY=gsk_your_key_here

mvn clean package -DskipTests

flink run -c com.example.ai.retail.job.InventoryReorderJob \
    target/flink-ai-app-*.jar
```
