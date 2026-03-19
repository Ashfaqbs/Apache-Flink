# ChurnDetectionJob — Real-Time Customer Churn Detection (ReAct Agent)

## What It Does

Analyzes customer behavioral events from Kafka topic `customer-events` in real time. A Groq LLM
running inside a ReAct reasoning loop assesses churn risk and classifies each customer as
**LOW / MEDIUM / HIGH / CRITICAL**.

For HIGH and CRITICAL customers the agent acts autonomously — **before** emitting any output it
calls retention tools in the same pipeline execution:

- `triggerRetentionOffer(customerId, offerType)` — sends a targeted discount or incentive to the
  customer via the Promotions / Offers Service.
- `notifyCRMSystem(customerId, churnRisk, signals)` — updates the CRM record with the current risk
  level and the signals that drove it (Salesforce / HubSpot).
- `escalateToHumanAgent(customerId, reason)` — raises a ticket for a human retention specialist
  (Zendesk / Freshdesk). This tool fires only for **CRITICAL** risk customers.

The final `ChurnAlert` is sinked to Kafka topic `churn-alerts`.

---

## What Problem It Solves

Traditional churn models run in batch — daily or weekly. By the time the model flags a customer,
they may have already churned. The intervention is always late and generic.

This pipeline detects churn signals in real time as behavioral events flow in — 28 days inactive,
3 support tickets, cart abandonment pattern, declining session frequency — and triggers personalized
interventions within seconds. The ReAct pattern means the LLM decides **which** retention offer is
most appropriate for the specific signals it detected, not a one-size-fits-all discount. A customer
flagged for long inactivity receives a re-engagement offer. One with multiple support tickets gets
an escalation path. The decision adapts to the evidence.

---

## Flow Diagram

```
[Kafka Source]
  Topic: customer-events
  Schema: { customerId, eventType, sessionCount,
            daysSinceLastPurchase, totalSpend, timestamp }
        |
        | eventType can be:
        | LOGIN / PURCHASE / BROWSE /
        | SUPPORT_TICKET / INACTIVITY /
        | CART_ABANDON
        v
+------------------------------------------+
|   ChurnDetectionAgent (ReAct — Groq)     |
|                                           |
|  Step 1: Assess churn risk               |
|          LOW / MEDIUM / HIGH / CRITICAL  |
|                                           |
|  Step 2: Identify churn signals          |
|    - long_inactivity (28+ days)          |
|    - multiple_support_tickets            |
|    - cart_abandon_pattern               |
|    - declining_session_count            |
|    - low_recent_spend                   |
|                                          |
|  Step 3 (if HIGH or CRITICAL):           |
|    -> triggerRetentionOffer(             |-----> [Promotions / Offers Service]
|         customerId, offerType)           |
|    -> notifyCRMSystem(                   |-----> [Salesforce / HubSpot API]
|         customerId, risk, signals)       |
|    -> escalateToHumanAgent(              |-----> [Zendesk / Freshdesk Ticket]
|         customerId, reason)             |
|         (if CRITICAL only)              |
|                                          |
|  Step 4: Emit ChurnAlert JSON            |
+------------------------------------------+
        |
        v
[Kafka Sink]
  Topic: churn-alerts
  Schema: { customerId, churnRisk, churnSignals[],
            actionTaken, timestamp }
        |
        v
  +----------+----------+----------+
  |          |          |          |
  v          v          v          v
Retention  CRM        Marketing  Customer
Dashboard  System     Automation  Success
                      (Emails)   Team
```

---

## Tool Decision Logic

| Risk | `triggerRetentionOffer` | `notifyCRMSystem` | `escalateToHumanAgent` |
|------|-------------------------|-------------------|------------------------|
| LOW | No | No | No |
| MEDIUM | No | No | No |
| HIGH | Yes | Yes | No |
| CRITICAL | Yes | Yes | Yes |

---

## Key Classes

| Class | Package | Role |
|-------|---------|------|
| `ChurnDetectionJob` | `retail.job` | Main entry point — wires Kafka source, agent, Kafka sink |
| `ChurnDetectionAgent` | `retail.agent` | ReAct agent — LLM reasoning + retention tool execution |
| `CustomerEvent` | `retail.dto` | Input DTO deserialized from Kafka |
| `ChurnAlert` | `retail.dto` | Output DTO serialized to Kafka |
| `CustomerEventDeserializationSchema` | `retail.job` | JSON → CustomerEvent |
| `ChurnAlertSerializer` | `retail.job` | ChurnAlert → JSON bytes |

---

## Kafka Topics

| Topic | Direction | Schema |
|-------|-----------|--------|
| `customer-events` | Source | `{ customerId, eventType, sessionCount, daysSinceLastPurchase, totalSpend, timestamp }` |
| `churn-alerts` | Sink | `{ customerId, churnRisk, churnSignals, actionTaken, timestamp }` |

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

4. **Produce test customer events** (from the repo root):
   ```bash
   python python/produce_customer_events.py
   ```

5. **Submit the job** — open the Flink UI at `http://localhost:8081`, upload the jar, and set the
   main class to:
   ```
   com.example.ai.retail.job.ChurnDetectionJob
   ```

6. **Monitor output** — consume from the `churn-alerts` topic:
   ```bash
   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
     --topic churn-alerts --from-beginning
   ```

---

## Sample Input / Output

### Input — high-risk customer event arriving on `customer-events`

A customer who has been inactive for 45 days, opened 2 support tickets recently, and just triggered
an INACTIVITY event:

```json
{
  "customerId": "CUST-9271",
  "eventType": "INACTIVITY",
  "sessionCount": 1,
  "daysSinceLastPurchase": 45,
  "totalSpend": 12.50,
  "timestamp": "2026-03-16T11:22:00Z"
}
```

The customer has not purchased in 45 days (well beyond the 28-day inactivity threshold), their
session count has dropped to 1, and recent spend is near zero. The agent sees converging signals
for HIGH churn risk.

### Output — alert emitted to `churn-alerts`

```json
{
  "customerId": "CUST-9271",
  "churnRisk": "HIGH",
  "churnSignals": [
    "long_inactivity",
    "declining_session_count",
    "low_recent_spend"
  ],
  "actionTaken": "retention_offer_triggered_crm_notified",
  "timestamp": "2026-03-16T11:22:01Z"
}
```

The agent identified three converging churn signals, classified the customer as HIGH risk, called
`triggerRetentionOffer` (sending a re-engagement discount) and `notifyCRMSystem` (updating the CRM
record) autonomously, and emitted the `ChurnAlert` — all within the same Flink operator invocation.
`escalateToHumanAgent` did not fire because the risk level was HIGH, not CRITICAL.
