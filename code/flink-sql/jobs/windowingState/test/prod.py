#!/usr/bin/env python3
import json
import time
import random
import string
from kafka import KafkaProducer

topic='topic-2'
producer = KafkaProducer(
    bootstrap_servers='localhost:9092',              
    key_serializer=lambda k: k.encode('utf-8'),       
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),  
    acks='all',                                       
    retries=5                                         
)

def make_order():
    """Generate a random order element with nested cJSON."""
    return {
        "itemId": ''.join(random.choices(string.digits, k=2)),
        "Itemname": random.choice(["Refrigerator", "Washer", "Oven", "Microwave", "Dishwasher"]),
        "ItemQty": random.randint(1, 5),
        "cJSON": {"cVar": str(random.randint(0, 9))}
    }

def make_user_payload():
    """Build a nested JSON payload with 1–5 orders."""
    num_orders = random.randint(1, 5)  
    return {
        "cEmail": random.choice([
            "alice@example.com", "bob@example.com", "charlie@example.com"
        ]),
        "CTimeStamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "orders": [make_order() for _ in range(num_orders)]
    }

try:
    while True:
        payload = make_user_payload()
        key = payload["cEmail"]

      
        producer.send(
            topic,
            key=key,
            value=payload
        )
        print(f"Sent to {topic} topic : {json.dumps(payload)}")

       
        producer.flush()  

        time.sleep(2)     

except KeyboardInterrupt:
    print("Shutting down producer...")

finally:
    producer.close()
