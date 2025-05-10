#!/usr/bin/env python3
import json
import time
import random
import string
from kafka import KafkaProducer

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    key_serializer=lambda k: k.encode('utf-8'),
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    acks='all',
    retries=5
)

def make_order():
    return {
        "orderid": ''.join(random.choices(string.ascii_lowercase + string.digits, k=6)),
        "orderName": random.choice(["phone", "laptop", "tablet", "watch", "headphones"]),
        "orderQty": random.randint(1, 10)
    }

def make_user_payload():
    num_orders = random.randint(1, 5)  # between 1 and 5 orders
    return {
        "username": random.choice(["ashu", "mike", "jane", "lisa", "tom"]),
        "id": f"id{random.randint(1,100)}",
        "orders": [make_order() for _ in range(num_orders)]
    }

try:
    inc = 0;
    while inc<=5:
        payload = make_user_payload()
        producer.send(
            topic='my-topic',
            key=payload['id'],
            value=payload
        )
        print(f"Sent: {json.dumps(payload)}")
        producer.flush()
        time.sleep(2)
        inc += 1
except KeyboardInterrupt:
    print("Producer stopped")

finally:
    producer.close()
