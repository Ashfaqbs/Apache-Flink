"""
Producer for FraudDetectionJob
================================
Sends Transaction events to Kafka topic: bank-transactions
Each message is consumed by FraudDetectionAgent (ReAct pattern) which
classifies fraud risk and calls blockCard / alertFraudTeam tools.

Schema:
{
  "transactionId": str,
  "accountId":     str,
  "amount":        float,
  "merchantCategory": str,
  "merchantLocation": str,
  "cardPresent":   bool,
  "timestamp":     str  (ISO-8601)
}

Run:
    pip install confluent-kafka
    python python/produce_bank_transactions.py
"""

import json
import random
import time
from datetime import datetime, timezone
from confluent_kafka import Producer

KAFKA_BROKER = "localhost:9092"
TOPIC = "bank-transactions"

# Mix of normal and suspicious transactions — designed to exercise all risk levels.
# The LLM will classify these based on amount, card presence, merchant, and location.
SAMPLE_TRANSACTIONS = [
    # --- LOW risk: normal domestic, card present ---
    {
        "merchantCategory": "grocery",
        "merchantLocation": "Mumbai, IN",
        "amount": 45.50,
        "cardPresent": True,
    },
    {
        "merchantCategory": "fuel",
        "merchantLocation": "Delhi, IN",
        "amount": 62.00,
        "cardPresent": True,
    },
    # --- MEDIUM risk: online purchase, moderate amount ---
    {
        "merchantCategory": "electronics_online",
        "merchantLocation": "Bangalore, IN",
        "amount": 850.00,
        "cardPresent": False,
    },
    {
        "merchantCategory": "clothing",
        "merchantLocation": "Chennai, IN",
        "amount": 320.00,
        "cardPresent": False,
    },
    # --- HIGH risk: large amount, card not present, foreign location ---
    {
        "merchantCategory": "luxury_goods",
        "merchantLocation": "Dubai, AE",
        "amount": 3200.00,
        "cardPresent": False,
    },
    {
        "merchantCategory": "casino",
        "merchantLocation": "Macau, MO",
        "amount": 5000.00,
        "cardPresent": False,
    },
    # --- CRITICAL risk: wire transfer, high value, high-risk country, no card ---
    {
        "merchantCategory": "wire_transfer",
        "merchantLocation": "Lagos, NG",
        "amount": 9800.00,
        "cardPresent": False,
    },
    {
        "merchantCategory": "crypto_exchange",
        "merchantLocation": "Unknown",
        "amount": 12500.00,
        "cardPresent": False,
    },
]

ACCOUNT_IDS = ["ACC-1001", "ACC-2034", "ACC-3872", "ACC-4491", "ACC-5120"]


def make_transaction(i: int) -> dict:
    """Builds a Transaction payload, cycling through the sample set."""
    template = SAMPLE_TRANSACTIONS[i % len(SAMPLE_TRANSACTIONS)]
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"
    return {
        "transactionId": f"TXN-{10000 + i:05d}",
        "accountId": random.choice(ACCOUNT_IDS),
        "amount": template["amount"] + round(random.uniform(-5, 5), 2),
        "merchantCategory": template["merchantCategory"],
        "merchantLocation": template["merchantLocation"],
        "cardPresent": template["cardPresent"],
        "timestamp": now,
    }


def delivery_report(err, msg):
    if err:
        print(f"  [ERROR] Delivery failed: {err}")
    else:
        print(f"  [OK]    Delivered to {msg.topic()} partition {msg.partition()}")


def main():
    producer = Producer({"bootstrap.servers": KAFKA_BROKER})
    print(f"[*] Sending bank transactions to '{TOPIC}' on {KAFKA_BROKER}")
    print(f"[*] Covers LOW / MEDIUM / HIGH / CRITICAL fraud scenarios\n")

    total = 24  # 3 full cycles through all 8 scenarios
    for i in range(total):
        txn = make_transaction(i)
        risk_hint = (
            "LOW" if txn["amount"] < 100
            else "MEDIUM" if txn["amount"] < 1000
            else "HIGH" if txn["amount"] < 5000
            else "CRITICAL"
        )
        print(f"[{i+1:02d}/{total}] Sending | {risk_hint:8s} hint | "
              f"${txn['amount']:>8.2f} | {txn['merchantCategory']:<25s} | "
              f"{txn['merchantLocation']}")

        producer.produce(
            TOPIC,
            key=txn["accountId"].encode("utf-8"),   # key by accountId for ordering
            value=json.dumps(txn).encode("utf-8"),
            callback=delivery_report,
        )
        producer.poll(0)

        # Space out messages so the LLM has time to process each one
        time.sleep(3)

    producer.flush()
    print(f"\n[*] Done. {total} transactions sent to '{TOPIC}'.")
    print("[*] Monitor fraud-alerts topic for LLM output.")


if __name__ == "__main__":
    main()
