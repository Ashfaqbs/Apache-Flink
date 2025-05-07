from kafka import KafkaProducer
import json
from faker import Faker
import random
import time
import uuid
from datetime import datetime

fake = Faker()
producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

topic_name = 'bank-transactions'

def generate_normal_transaction():
    return {
        "transactionId": str(uuid.uuid4()),
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "accountId": fake.bban(),
        "sourceAccount": fake.iban(),
        "destinationAccount": fake.iban(),
        "amount": round(random.uniform(50, 500), 2),
        "currency": "USD",
        "transactionType": random.choice(["POS_PURCHASE", "BILL_PAYMENT", "TRANSFER"]),
        "channel": random.choice(["MOBILE_APP", "WEB", "ATM", "CARD_TERMINAL"]),
        "location": {
            "ip": fake.ipv4_public(),
            "city": fake.city(),
            "country": fake.country_code()
        },
        "status": "COMPLETED"
    }

def generate_fraud_transaction():
    reasons = [
        "HighRiskCountry + UnusualAmount",
        "VelocityPatternDetected",
        "NewDeviceLogin + HighAmount"
    ]
    return {
        "transactionId": str(uuid.uuid4()),
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "accountId": fake.bban(),
        "sourceAccount": fake.iban(),
        "destinationAccount": fake.iban(),
        "amount": round(random.uniform(3000, 10000), 2),
        "currency": "USD",
        "transactionType": "TRANSFER",
        "channel": "MOBILE_APP",
        "location": {
            "ip": fake.ipv4_public(),
            "city": fake.city(),
            "country": fake.country_code()
        },
        "status": "BLOCKED",
        "fraudFlag": True,
        "fraudReason": random.choice(reasons),
        "fraudScore": round(random.uniform(0.85, 0.99), 2),
        "verdict": random.choice(["FRAUD", "REVIEW"])
    }

# Create a mixed list of normal and fraud transactions
transactions = []

# 8 normal, 12 fraud (customize the split as needed)
for _ in range(8):
    transactions.append(generate_normal_transaction())

for _ in range(12):
    transactions.append(generate_fraud_transaction())

# Shuffle for randomness
random.shuffle(transactions)

# Send to Kafka
for tx in transactions:
    print(f"Sending: {json.dumps(tx, indent=2)}")
    producer.send(topic_name, value=tx)
    time.sleep(0.3)

producer.flush()
producer.close()
print("✅ All transactions sent.")
