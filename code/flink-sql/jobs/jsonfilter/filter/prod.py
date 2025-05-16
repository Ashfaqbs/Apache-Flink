#!/usr/bin/env python3
import json
import time
import random
import string
from kafka import KafkaProducer
from faker import Faker

fake = Faker()

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    key_serializer=lambda k: k.encode('utf-8'),
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    acks='all',
    retries=5
)

def make_order_line():
    return {
        "itemId": ''.join(random.choices(string.digits, k=2)),
        "Itemname": random.choice(["Refrigerator", "Washer", "Oven", "Microwave", "Dishwasher"]),
        "itemLongDesc": "owes Delivery Service Item",
        "orderLineKey": ''.join(random.choices(string.digits, k=26)),
        "ItemQty": random.randint(1, 5),
        "shipNode": {
            "nodeId": random.choice(["1983", "1984", "1985"]),
            "nodeType": "Store"
        }
    }

def make_payload():
    return {
        "customerEmailId": fake.email(),
        "customerFirstName": fake.first_name(),
        "customerLastName": fake.last_name(),
        "customerPhoneNumber": ''.join(random.choices(string.digits, k=10)),
        "customerPoNo": fake.company(),
        "customerType": random.choice(["PRO", "INDIVIDUAL"]),
        "customerCategory": random.choice(["Commercial Contractor", "Retail Customer"]),
        "orderLines": [make_order_line() for _ in range(random.randint(1, 5))]
    }

for i in range(5):
    payload = make_payload()
    key = payload["customerEmailId"]

    producer.send(
        topic='source-a',
        key=key,
        value=payload
    )
    
    print(f"Sent: {json.dumps(payload)}")

    time.sleep(1)

producer.flush()
producer.close()
print(" All messages sent.")
