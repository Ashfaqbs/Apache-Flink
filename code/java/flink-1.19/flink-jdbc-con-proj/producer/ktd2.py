from kafka import KafkaProducer
import json
import time
import random

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

names = ['John', 'Alice', 'Raj', 'Maria', 'Wei']
countries = ['USA', 'Canada', 'India', 'Germany', 'China']
visa_types = ['Tourist', 'Work', 'Student']

while True:
    data = {
        "id": random.randint(1, 100),
        "name": random.choice(names),
        "country": random.choice(countries),
        "visaType": random.choice(visa_types)
    }
    producer.send('input-topic', value=data)
    print(f"Sent: {data}")
    time.sleep(1)
