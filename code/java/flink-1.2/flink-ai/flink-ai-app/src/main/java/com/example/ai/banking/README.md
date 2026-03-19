# Banking AI Pipelines

Two real-time AI-powered Flink pipelines for banking use cases, built with Flink Agents and Groq LLM.

---

## FraudDetectionJob

### What It Does
Analyzes every incoming bank transaction in real time and classifies it as LOW, MEDIUM, HIGH, or CRITICAL fraud risk. For HIGH and CRITICAL transactions, the LLM autonomously calls tools to block the customer's card and alert the fraud operations team — before the transaction can clear.

### Problem It Solves
Traditional rule-based fraud detection (velocity checks, static thresholds) misses novel attack patterns. This pipeline uses an LLM with a ReAct reasoning loop to evaluate the full context of each transaction — amount, merchant category, location, card presence — and makes nuanced decisions that adapt to new fraud patterns without redeploying rules.

### Flow

```
[Kafka: bank-transactions]
        |
        | JSON: { transactionId, accountId, amount, merchantCategory,
        |         merchantLocation, cardPresent, timestamp }
        v
[FraudDetectionAgent — Groq LLM (ReAct)]
        |
        |  Step 1: Assess fraud risk level (LOW/MEDIUM/HIGH/CRITICAL)
        |  Step 2: Identify fraud signals
        |  Step 3 (if HIGH/CRITICAL):
        |    -> blockCard(accountId, reason)       [Tool]
        |    -> alertFraudTeam(details, severity)  [Tool]
        |  Step 4: Emit structured JSON response
        v
[FraudAlert: { transactionId, accountId, riskLevel,
               fraudSignals, actionTaken, timestamp }]
        |
        v
[Kafka: fraud-alerts]
        |
        v
[Downstream: fraud ops dashboard, card management system, compliance]
```

### Key Classes
| Class | Role |
|-------|------|
| `FraudDetectionAgent` | ReAct LLM agent — reasons and calls tools |
| `Transaction` | Input DTO from Kafka |
| `FraudAlert` | Output DTO to Kafka |
| `TransactionDeserializationSchema` | Kafka JSON deserializer |
| `FraudAlertSerializer` | Kafka JSON serializer |
| `FraudDetectionJob` | Main entry point |

### Kafka Topics
| Topic | Direction | Description |
|-------|-----------|-------------|
| `bank-transactions` | Source | Raw transactions from core banking |
| `fraud-alerts` | Sink | AI-generated fraud assessments |

### How to Run
```bash
# Set the Groq API key
export GROQ_API_KEY=gsk_your_key_here

# Build the fat jar
mvn clean package -DskipTests

# Submit to Flink cluster
flink run -c com.example.ai.banking.job.FraudDetectionJob \
    target/flink-ai-app-*.jar
```

---

## LoanRiskPipelineJob

### What It Does
A two-stage AI pipeline that processes loan applications from Kafka. Agent 1 extracts a risk score and risk factors per application. A 5-minute tumbling window aggregates all applications for the same applicant. Agent 2 then generates a final underwriting decision (APPROVE / REVIEW / DECLINE) with justification. If the decision is REVIEW, a human underwriter is automatically notified.

### Problem It Solves
Manual loan underwriting is slow, inconsistent, and expensive. This pipeline provides a first-pass automated risk assessment that handles straightforward approvals and declines automatically, while routing borderline cases to human underwriters with pre-computed risk context — reducing review time and improving consistency.

### Flow

```
[Kafka: loan-applications]
        |
        | JSON: { applicationId, applicantId, requestedAmount, creditScore,
        |         annualIncome, employmentYears, existingDebtAmount, timestamp }
        v
[Agent 1: LoanRiskExtractionAgent — Groq LLM (Workflow)]
        |
        |  - Assigns risk score 1-10
        |  - Identifies factors: credit score, DTI ratio, employment history
        |  - Emits: LoanRiskSignals { applicationId, applicantId, riskScore,
        |           riskFactors, approvalRecommendation }
        v
[Tumbling Window: 5 minutes, keyed by applicantId]
        |
        |  - Averages risk scores across applications
        |  - Unions all risk factors
        |  - Collects per-application recommendations
        |  - Outputs: LoanWindowSummary JSON
        v
[Agent 2: LoanRiskReportAgent — Groq LLM (Workflow)]
        |
        |  - Generates final decision: APPROVE / REVIEW / DECLINE
        |  - If REVIEW: calls notifyUnderwriter(applicantId, summary) [Tool]
        |  - Emits: LoanRiskReport { applicantId, applicationCount,
        |           finalDecision, justification, avgRiskScore }
        v
[Kafka: loan-risk-reports]
        |
        v
[Downstream: loan origination system, compliance, underwriter dashboard]
```

### Key Classes
| Class | Role |
|-------|------|
| `LoanRiskExtractionAgent` | Agent 1 — per-application risk extraction |
| `LoanRiskReportAgent` | Agent 2 — final underwriting decision |
| `LoanApplication` | Input DTO |
| `LoanRiskSignals` | Agent 1 output, window key type |
| `LoanWindowSummary` | Aggregated window data for Agent 2 |
| `LoanRiskReport` | Final output DTO |
| `LoanRiskPipelineJob` | Main entry point with window function |

### Kafka Topics
| Topic | Direction | Description |
|-------|-----------|-------------|
| `loan-applications` | Source | Applications from the banking portal |
| `loan-risk-reports` | Sink | Final AI-generated underwriting decisions |

### How to Run
```bash
export GROQ_API_KEY=gsk_your_key_here

mvn clean package -DskipTests

flink run -c com.example.ai.banking.job.LoanRiskPipelineJob \
    target/flink-ai-app-*.jar
```
