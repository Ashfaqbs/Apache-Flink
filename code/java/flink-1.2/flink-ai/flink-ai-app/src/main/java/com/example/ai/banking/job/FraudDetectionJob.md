# FraudDetectionJob — Real-Time Bank Fraud Detection (ReAct Agent)

## What It Does

Analyzes every bank transaction coming from Kafka topic `bank-transactions` in real time. A Groq LLM
running inside a ReAct reasoning loop classifies each transaction as **LOW / MEDIUM / HIGH / CRITICAL**
fraud risk.

For HIGH and CRITICAL transactions the LLM acts autonomously — **before** emitting any output it calls
two tools in the same pipeline execution:

- `blockCard(accountId, reason)` — suspends the card via the Card Management API.
- `alertFraudTeam(incidentDetails, severity)` — pages the fraud operations team via PagerDuty / Email /
  Slack.

The enriched `FraudAlert` is then sinked to Kafka topic `fraud-alerts` for all downstream consumers.

---

## What Problem It Solves

Traditional rule-based fraud detection relies on static velocity thresholds and known attack patterns.
It cannot adapt to novel fraud techniques without a full rule redeployment cycle, and it evaluates
signals independently rather than as a coherent narrative.

An LLM-based ReAct agent evaluates the **full context** of each transaction simultaneously — amount,
merchant category, card presence, location, and time — and reasons through the fraud signals the same
way a human analyst would. The ReAct loop means that if the model decides an action is warranted it
takes it immediately: cards get blocked in the **same pipeline execution**, not hours later via a batch
report or manual review queue.

---

## Flow Diagram

```
[Kafka Source]
  Topic: bank-transactions
  Schema: { transactionId, accountId, amount,
            merchantCategory, merchantLocation,
            cardPresent, timestamp }
        |
        v
+---------------------------------------+
|   FraudDetectionAgent (ReAct — Groq)  |
|                                       |
|  Step 1: Assess risk level            |
|          LOW / MEDIUM / HIGH          |
|          / CRITICAL                   |
|                                       |
|  Step 2: Identify fraud signals       |
|    - card_not_present                 |
|    - high_value_foreign_txn           |
|    - high_risk_merchant_category      |
|    - velocity_spike                   |
|    - unusual_location                 |
|                                       |
|  Step 3 (if HIGH or CRITICAL):        |
|    -> blockCard(accountId, reason)    |-----> [Card Management API]
|    -> alertFraudTeam(details, sev)   |-----> [PagerDuty / Email / Slack]
|                                       |
|  Step 4: Emit FraudAlert JSON         |
+---------------------------------------+
        |
        v
[Kafka Sink]
  Topic: fraud-alerts
  Schema: { transactionId, accountId, riskLevel,
            fraudSignals[], actionTaken, timestamp }
        |
        v
  +----------+----------+----------+
  |          |          |          |
  v          v          v          v
Fraud Ops  Card Mgmt  Compliance  Audit
Dashboard  System     Reporting   Log
```

---

## When Each Tool Fires

| Risk Level | `blockCard` called | `alertFraudTeam` called |
|------------|-------------------|------------------------|
| LOW        | No                | No                     |
| MEDIUM     | No                | No                     |
| HIGH       | Yes               | Yes                    |
| CRITICAL   | Yes               | Yes                    |

---

## Key Classes

| Class | Package | Role |
|-------|---------|------|
| `FraudDetectionJob` | `banking.job` | Main entry point — wires Kafka source, agent, Kafka sink |
| `FraudDetectionAgent` | `banking.agent` | ReAct agent — LLM reasoning + tool execution |
| `Transaction` | `banking.dto` | Input DTO deserialized from Kafka |
| `FraudAlert` | `banking.dto` | Output DTO serialized to Kafka |
| `TransactionDeserializationSchema` | `banking.job` | JSON → Transaction |
| `FraudAlertSerializer` | `banking.job` | FraudAlert → JSON bytes |

---

## Kafka Topics

| Topic | Direction | Schema |
|-------|-----------|--------|
| `bank-transactions` | Source | `{ transactionId, accountId, amount, merchantCategory, merchantLocation, cardPresent, timestamp }` |
| `fraud-alerts` | Sink | `{ transactionId, accountId, riskLevel, fraudSignals, actionTaken, timestamp }` |

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

4. **Produce test transactions** (from the repo root):
   ```bash
   python python/produce_bank_transactions.py
   ```

5. **Submit the job** — open the Flink UI at `http://localhost:8081`, upload the jar, and set the
   main class to:
   ```
   com.example.ai.banking.job.FraudDetectionJob
   ```

6. **Monitor output** — consume from the `fraud-alerts` topic:
   ```bash
   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
     --topic fraud-alerts --from-beginning
   ```

---

## Sample Input / Output

### Input — transaction arriving on `bank-transactions`

```json
{
  "transactionId": "TXN-8821",
  "accountId": "ACC-4492",
  "amount": 4850.00,
  "merchantCategory": "wire_transfer",
  "merchantLocation": "Lagos, NG",
  "cardPresent": false,
  "timestamp": "2026-03-16T07:15:00Z"
}
```

### Output — alert emitted to `fraud-alerts`

```json
{
  "transactionId": "TXN-8821",
  "accountId": "ACC-4492",
  "riskLevel": "CRITICAL",
  "fraudSignals": [
    "card_not_present",
    "high_value_foreign_transfer",
    "high_risk_country"
  ],
  "actionTaken": "card_blocked_fraud_team_alerted",
  "timestamp": "2026-03-16T07:15:01Z"
}
```

The agent identified three converging fraud signals (card-not-present + high-value + high-risk
geography), classified the transaction as CRITICAL, called `blockCard` and `alertFraudTeam`
autonomously, and emitted the `FraudAlert` — all within the same Flink operator invocation.
