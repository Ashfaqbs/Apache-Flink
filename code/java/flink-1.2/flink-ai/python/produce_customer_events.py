"""
Producer for ChurnDetectionJob
================================
Sends CustomerEvent events to Kafka topic: customer-events
Each message is consumed by ChurnDetectionAgent (ReAct pattern) which
assesses churn risk and calls triggerRetentionOffer / notifyCRMSystem /
escalateToHumanAgent tools when risk is HIGH or CRITICAL.

Schema:
{
  "customerId":            str,
  "eventType":             str  (LOGIN/PURCHASE/BROWSE/SUPPORT_TICKET/INACTIVITY/CART_ABANDON),
  "sessionCount":          int,
  "daysSinceLastPurchase": int,
  "totalSpend":            float,
  "timestamp":             str  (ISO-8601)
}

Run:
    pip install confluent-kafka
    python python/produce_customer_events.py
"""

import json
import time
from datetime import datetime, timezone
from confluent_kafka import Producer

KAFKA_BROKER = "localhost:9092"
TOPIC = "customer-events"

# Each entry represents one customer behavioral event.
# The combination of eventType, daysSinceLastPurchase, sessionCount, and totalSpend
# paints a picture the LLM uses to classify churn risk.
SAMPLE_EVENTS = [
    # --- LOW risk: active, loyal, recent purchase ---
    {
        "customerId": "USR-1001",
        "eventType": "PURCHASE",
        "sessionCount": 14,
        "daysSinceLastPurchase": 3,
        "totalSpend": 4200.00,
        "expected_risk": "LOW",
    },
    {
        "customerId": "USR-1002",
        "eventType": "LOGIN",
        "sessionCount": 22,
        "daysSinceLastPurchase": 7,
        "totalSpend": 8750.50,
        "expected_risk": "LOW",
    },
    # --- MEDIUM risk: starting to go quiet, occasional browse only ---
    {
        "customerId": "USR-2001",
        "eventType": "BROWSE",
        "sessionCount": 4,
        "daysSinceLastPurchase": 21,
        "totalSpend": 1200.00,
        "expected_risk": "MEDIUM",
    },
    {
        "customerId": "USR-2002",
        "eventType": "CART_ABANDON",
        "sessionCount": 3,
        "daysSinceLastPurchase": 18,
        "totalSpend": 950.00,
        "expected_risk": "MEDIUM",
    },
    # --- HIGH risk: long inactivity + support tickets — retention tools should fire ---
    {
        "customerId": "USR-3001",
        "eventType": "SUPPORT_TICKET",
        "sessionCount": 1,
        "daysSinceLastPurchase": 45,
        "totalSpend": 620.00,
        "expected_risk": "HIGH",
    },
    {
        "customerId": "USR-3002",
        "eventType": "INACTIVITY",
        "sessionCount": 0,
        "daysSinceLastPurchase": 38,
        "totalSpend": 310.00,
        "expected_risk": "HIGH",
    },
    # --- CRITICAL risk: long inactivity + multiple support tickets + near zero spend ---
    # These should trigger triggerRetentionOffer + notifyCRMSystem + escalateToHumanAgent
    {
        "customerId": "USR-4001",
        "eventType": "SUPPORT_TICKET",
        "sessionCount": 0,
        "daysSinceLastPurchase": 72,
        "totalSpend": 85.00,
        "expected_risk": "CRITICAL",
    },
    {
        "customerId": "USR-4002",
        "eventType": "INACTIVITY",
        "sessionCount": 0,
        "daysSinceLastPurchase": 90,
        "totalSpend": 0.00,
        "expected_risk": "CRITICAL",
    },
]


def make_event(profile: dict) -> dict:
    """Builds a CustomerEvent payload from a profile template."""
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"
    return {
        "customerId": profile["customerId"],
        "eventType": profile["eventType"],
        "sessionCount": profile["sessionCount"],
        "daysSinceLastPurchase": profile["daysSinceLastPurchase"],
        "totalSpend": profile["totalSpend"],
        "timestamp": now,
    }


def delivery_report(err, msg):
    if err:
        print(f"  [ERROR] Delivery failed: {err}")
    else:
        print(f"  [OK]    Delivered to {msg.topic()} partition {msg.partition()}")


def main():
    producer = Producer({"bootstrap.servers": KAFKA_BROKER})
    print(f"[*] Sending customer events to '{TOPIC}' on {KAFKA_BROKER}")
    print(f"[*] Covers LOW / MEDIUM / HIGH / CRITICAL churn risk scenarios\n")

    total = len(SAMPLE_EVENTS)
    for i, profile in enumerate(SAMPLE_EVENTS):
        event = make_event(profile)
        print(f"[{i+1:02d}/{total}] Sending | Expected: {profile['expected_risk']:8s} | "
              f"Customer: {profile['customerId']:<10s} | "
              f"EventType: {profile['eventType']:<15s} | "
              f"DaysSincePurchase: {profile['daysSinceLastPurchase']}")

        producer.produce(
            TOPIC,
            key=event["customerId"].encode("utf-8"),
            value=json.dumps(event).encode("utf-8"),
            callback=delivery_report,
        )
        producer.poll(0)

        # Give the LLM time between requests — Groq free tier has rate limits
        time.sleep(4)

    producer.flush()
    print(f"\n[*] Done. {total} customer events sent to '{TOPIC}'.")
    print("[*] Monitor churn-alerts topic for LLM output.")
    print("[*] HIGH/CRITICAL events should show tool calls in Flink TaskManager logs.")


if __name__ == "__main__":
    main()
