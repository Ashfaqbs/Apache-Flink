from kafka import KafkaProducer
import json
from faker import Faker
import random
import time


# change the topic as per job

fake = Faker()
producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

roles = ['developer', 'manager', 'analyst', 'tester', 'architect']

for _ in range(10):
    data = {
        "name": fake.name(),
        "email": fake.email(),
        "role": random.choice(roles)
    }
    print(f"Sending: {data}")
    producer.send('my-topic', value=data)
    time.sleep(0.5)  

producer.flush()
print(" All messages sent.")
producer.close()