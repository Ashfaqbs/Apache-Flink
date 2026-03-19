# LoanRiskPipelineJob — Automated Loan Risk Assessment (Multi-Agent Workflow)

## What It Does

A two-stage AI pipeline that automates first-pass loan underwriting.

**Stage 1 — Per-application scoring** (`LoanRiskExtractionAgent`): reads each loan application from
Kafka topic `loan-applications` and produces a risk score (1–10) plus a list of specific risk factors
(`LoanRiskSignals`).

**Windowing**: a 5-minute tumbling window keys by `applicantId` and aggregates all applications seen
in that window into a `LoanWindowSummary` — averaging scores, unioning risk factors, collecting
recommendations, and counting submissions.

**Stage 2 — Final underwriting decision** (`LoanRiskReportAgent`): receives the `LoanWindowSummary`
and produces a final decision — **APPROVE**, **REVIEW**, or **DECLINE** — with a written justification.
If the decision is REVIEW, the agent calls the `notifyUnderwriter(applicantId, summary)` tool to route
the case to a human. The final `LoanRiskReport` is sinked to Kafka topic `loan-risk-reports`.

---

## What Problem It Solves

Manual loan underwriting is slow (days to weeks), inconsistent across analysts, and expensive at scale.

This pipeline provides an automated first-pass assessment that:

- Handles clear-cut approvals and declines **instantly**, without any human in the loop.
- Routes genuinely ambiguous cases to human underwriters with a **pre-computed risk summary** — the
  underwriter sees the model's analysis, not just the raw application.
- Catches **pattern issues across submissions**: the tumbling window aggregation detects when an
  applicant submits multiple applications within minutes with slightly different income or debt figures
  — a common application-fraud signal that per-record analysis would miss entirely.

---

## Flow Diagram

```
[Kafka Source]
  Topic: loan-applications
  Schema: { applicationId, applicantId, requestedAmount,
            creditScore, annualIncome, employmentYears,
            existingDebtAmount, timestamp }
        |
        v
+------------------------------------------+
|  Agent 1: LoanRiskExtractionAgent         |
|  (Workflow — Groq)                        |
|                                           |
|  Per application:                         |
|  - Assign risk score 1-10                 |
|  - Identify risk factors:                 |
|    low_credit_score / high_dti_ratio /    |
|    short_employment / high_requested_amt  |
|  - Recommend: APPROVE / REVIEW / DECLINE  |
|                                           |
|  Output: LoanRiskSignals                  |
|  { applicationId, applicantId,            |
|    riskScore, riskFactors[],              |
|    approvalRecommendation }               |
+------------------------------------------+
        |
        v keyed by applicantId
+------------------------------------------+
|  Tumbling Window — 5 minutes             |
|                                           |
|  Per applicant per window:               |
|  - Average risk scores                    |
|  - Union all risk factors                 |
|  - Collect all recommendations           |
|  - Count applications seen               |
|                                           |
|  Output: LoanWindowSummary JSON           |
+------------------------------------------+
        |
        v
+------------------------------------------+
|  Agent 2: LoanRiskReportAgent             |
|  (Workflow — Groq)                        |
|                                           |
|  - Final decision: APPROVE/REVIEW/DECLINE |
|  - Generate justification list           |
|  - If REVIEW:                             |
|    -> notifyUnderwriter(applicantId, sum) |-----> [Email / Internal System]
|                                           |
|  Output: LoanRiskReport                  |
|  { applicantId, applicationCount,         |
|    finalDecision, justification[],        |
|    avgRiskScore }                         |
+------------------------------------------+
        |
        v
[Kafka Sink]
  Topic: loan-risk-reports
        |
        v
  +----------+----------+----------+
  |          |          |          |
  v          v          v          v
Loan       Under-    Compliance  Core
Origination writer   Reporting  Banking
System     Portal               System
```

---

## Decision Matrix

| Avg Risk Score | Typical Final Decision | Notes |
|---------------|----------------------|-------|
| 1 – 3 | APPROVE | Low risk across all factors |
| 4 – 6 | REVIEW | Ambiguous signals; routed to underwriter |
| 7 – 10 | DECLINE | High risk — multiple compounding factors |

The LLM may override the score-based default when specific factor combinations warrant it. For example,
a score of 4 paired with `low_credit_score` + `high_dti_ratio` + `short_employment` simultaneously
may push the agent to DECLINE rather than REVIEW.

---

## Key Classes

| Class | Package | Role |
|-------|---------|------|
| `LoanRiskPipelineJob` | `banking.job` | Main entry point — wires Kafka source, both agents, window, Kafka sink |
| `LoanRiskExtractionAgent` | `banking.agent` | Stage 1 agent — scores each application and extracts risk factors |
| `LoanRiskReportAgent` | `banking.agent` | Stage 2 agent — makes final underwriting decision; calls `notifyUnderwriter` if REVIEW |
| `LoanApplication` | `banking.dto` | Input DTO deserialized from Kafka |
| `LoanRiskSignals` | `banking.dto` | Intermediate DTO — per-application score and risk factors from Agent 1 |
| `LoanWindowSummary` | `banking.dto` | Aggregated window result passed to Agent 2 |
| `LoanRiskReport` | `banking.dto` | Final output DTO serialized to Kafka |
| `LoanApplicationDeserializationSchema` | `banking.job` | JSON → LoanApplication |
| `LoanRiskReportSerializer` | `banking.job` | LoanRiskReport → JSON bytes |

---

## Kafka Topics

| Topic | Direction | Schema |
|-------|-----------|--------|
| `loan-applications` | Source | `{ applicationId, applicantId, requestedAmount, creditScore, annualIncome, employmentYears, existingDebtAmount, timestamp }` |
| `loan-risk-reports` | Sink | `{ applicantId, applicationCount, finalDecision, justification, avgRiskScore }` |

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

4. **Produce test loan applications** (from the repo root):
   ```bash
   python python/produce_loan_applications.py
   ```

5. **Submit the job** — open the Flink UI at `http://localhost:8081`, upload the jar, and set the
   main class to:
   ```
   com.example.ai.banking.job.LoanRiskPipelineJob
   ```

6. **Monitor output** — consume from the `loan-risk-reports` topic:
   ```bash
   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
     --topic loan-risk-reports --from-beginning
   ```

> **Window timing note**: results only appear after the 5-minute tumbling window closes. For local
> development and demos, reduce the window duration to 30 seconds by changing `Duration.ofMinutes(5)`
> to `Duration.ofSeconds(30)` in `LoanRiskPipelineJob.java`.

---

## Sample Input / Output

### Input — application arriving on `loan-applications`

```json
{
  "applicationId": "APP-3341",
  "applicantId": "CUST-7820",
  "requestedAmount": 45000.00,
  "creditScore": 580,
  "annualIncome": 32000.00,
  "employmentYears": 0.8,
  "existingDebtAmount": 18500.00,
  "timestamp": "2026-03-16T09:30:00Z"
}
```

This applicant has a below-prime credit score (580), a debt-to-income ratio above 50 %, less than
one year of employment history, and is requesting an amount that is more than their annual income.

### Output — report emitted to `loan-risk-reports`

```json
{
  "applicantId": "CUST-7820",
  "applicationCount": 1,
  "finalDecision": "DECLINE",
  "justification": [
    "Credit score of 580 is below the acceptable threshold of 620",
    "Debt-to-income ratio of 57.8% significantly exceeds the 43% guideline",
    "Employment tenure of 0.8 years indicates insufficient income stability",
    "Requested amount of $45,000 exceeds annual income of $32,000"
  ],
  "avgRiskScore": 8.5
}
```

Four compounding risk factors drove the agent to DECLINE without routing to a human underwriter —
the signal combination is unambiguous enough that no further review would be productive.
