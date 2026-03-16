"""
Producer for LoanRiskPipelineJob
==================================
Sends LoanApplication events to Kafka topic: loan-applications
Each message is consumed by LoanRiskExtractionAgent (Agent 1) which scores
the application. After the 5-minute tumbling window closes, LoanRiskReportAgent
(Agent 2) generates the final APPROVE / REVIEW / DECLINE decision.

Schema:
{
  "applicationId":     str,
  "applicantId":       str,
  "requestedAmount":   float,
  "creditScore":       int,
  "annualIncome":      float,
  "employmentYears":   int,
  "existingDebtAmount":float,
  "timestamp":         str  (ISO-8601)
}

Tip: The window is 5 minutes. To see results faster during development,
     change Duration.ofMinutes(5) to Duration.ofSeconds(30) in LoanRiskPipelineJob.java.

Run:
    pip install confluent-kafka
    python python/produce_loan_applications.py
"""

import json
import time
import itertools
from datetime import datetime, timezone
from confluent_kafka import Producer

KAFKA_BROKER = "localhost:9092"
TOPIC = "loan-applications"

# Grouped by expected outcome — multiple applications per applicant to exercise the window.
# Same applicantId within a short window triggers the aggregation logic.
APPLICANT_PROFILES = [
    # --- APPROVE profile: strong credit, low DTI, stable employment ---
    {
        "applicantId": "CUST-A001",
        "requestedAmount": 25000.0,
        "creditScore": 780,
        "annualIncome": 95000.0,
        "employmentYears": 8,
        "existingDebtAmount": 5000.0,
        "expected": "APPROVE",
    },
    # --- APPROVE profile: modest loan, very strong financials ---
    {
        "applicantId": "CUST-A002",
        "requestedAmount": 10000.0,
        "creditScore": 810,
        "annualIncome": 72000.0,
        "employmentYears": 12,
        "existingDebtAmount": 2000.0,
        "expected": "APPROVE",
    },
    # --- REVIEW profile: borderline credit, moderate DTI ---
    {
        "applicantId": "CUST-B001",
        "requestedAmount": 40000.0,
        "creditScore": 660,
        "annualIncome": 55000.0,
        "employmentYears": 3,
        "existingDebtAmount": 18000.0,
        "expected": "REVIEW",
    },
    # --- REVIEW profile: decent income but low credit score ---
    {
        "applicantId": "CUST-B001",   # same applicant — second application in window
        "requestedAmount": 35000.0,
        "creditScore": 645,
        "annualIncome": 55000.0,
        "employmentYears": 3,
        "existingDebtAmount": 20000.0,
        "expected": "REVIEW",
    },
    # --- DECLINE profile: poor credit, high DTI, short employment ---
    {
        "applicantId": "CUST-C001",
        "requestedAmount": 80000.0,
        "creditScore": 540,
        "annualIncome": 32000.0,
        "employmentYears": 1,
        "existingDebtAmount": 28000.0,
        "expected": "DECLINE",
    },
    # --- DECLINE profile: multiple high-risk signals ---
    {
        "applicantId": "CUST-C002",
        "requestedAmount": 120000.0,
        "creditScore": 510,
        "annualIncome": 28000.0,
        "employmentYears": 0,
        "existingDebtAmount": 35000.0,
        "expected": "DECLINE",
    },
]

counter = itertools.count(1)


def make_application(profile: dict) -> dict:
    """Builds a LoanApplication payload from a profile template."""
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"
    app_id = f"APP-{next(counter):05d}"
    return {
        "applicationId": app_id,
        "applicantId": profile["applicantId"],
        "requestedAmount": profile["requestedAmount"],
        "creditScore": profile["creditScore"],
        "annualIncome": profile["annualIncome"],
        "employmentYears": profile["employmentYears"],
        "existingDebtAmount": profile["existingDebtAmount"],
        "timestamp": now,
    }


def delivery_report(err, msg):
    if err:
        print(f"  [ERROR] Delivery failed: {err}")
    else:
        print(f"  [OK]    Delivered to {msg.topic()} partition {msg.partition()}")


def main():
    producer = Producer({"bootstrap.servers": KAFKA_BROKER})
    print(f"[*] Sending loan applications to '{TOPIC}' on {KAFKA_BROKER}")
    print(f"[*] Covers APPROVE / REVIEW / DECLINE profiles")
    print(f"[*] Results appear after the 5-minute window closes\n")

    for profile in APPLICANT_PROFILES:
        app = make_application(profile)
        dti = round(profile["existingDebtAmount"] / profile["annualIncome"] * 100, 1)
        print(f"Sending | Expected: {profile['expected']:7s} | "
              f"Applicant: {profile['applicantId']:<12s} | "
              f"CreditScore: {profile['creditScore']} | "
              f"DTI: {dti}%")

        producer.produce(
            TOPIC,
            key=profile["applicantId"].encode("utf-8"),  # key by applicantId for windowing
            value=json.dumps(app).encode("utf-8"),
            callback=delivery_report,
        )
        producer.poll(0)

        # Space out messages — the window aggregates by applicantId
        time.sleep(5)

    producer.flush()
    print(f"\n[*] Done. {len(APPLICANT_PROFILES)} applications sent to '{TOPIC}'.")
    print("[*] Wait for window to close, then monitor loan-risk-reports topic.")


if __name__ == "__main__":
    main()
