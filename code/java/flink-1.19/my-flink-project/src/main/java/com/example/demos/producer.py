from kafka import KafkaProducer
import json
import time
import random

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

user_ids = ['u1', 'u2', 'u3']
actions = ['CLICK', 'VIEW', 'ADD_TO_CART']

for i in range(20):
    event = {
        "userId": random.choice(user_ids),
        "action": random.choice(actions)
    }
    producer.send("input-topic", event)
    print(f"Sent: {event}")
    time.sleep(1)  # simulate delay

producer.flush()
producer.close()
