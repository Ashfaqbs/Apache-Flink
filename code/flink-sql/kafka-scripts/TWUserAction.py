from kafka import KafkaProducer
import json
from datetime import datetime

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

events = [
    {"username": "Mary", "event_time": "2025-05-07T12:00:00", "page_url": "./home"},
    {"username": "Bob", "event_time": "2025-05-07T12:00:00", "page_url": "./cart"},
    {"username": "Mary", "event_time": "2025-05-07T12:02:00", "page_url": "./prod?id=1"},
    {"username": "Mary", "event_time": "2025-05-07T12:55:00", "page_url": "./prod?id=4"},
    {"username": "Bob", "event_time": "2025-05-07T13:01:00", "page_url": "./prod?id=5"},
    {"username": "Liz", "event_time": "2025-05-07T13:30:00", "page_url": "./home"},
    {"username": "Liz", "event_time": "2025-05-07T13:59:00", "page_url": "./prod?id=7"},
    {"username": "Mary", "event_time": "2025-05-07T14:00:00", "page_url": "./cart"},
    {"username": "Liz", "event_time": "2025-05-07T14:02:00", "page_url": "./home"},
    {"username": "Bob", "event_time": "2025-05-07T14:30:00", "page_url": "./prod?id=3"},
    {"username": "Bob", "event_time": "2025-05-07T14:40:00", "page_url": "./home"}
]

for event in events:
    producer.send('user-topic', value=event)

producer.flush()
print("All events sent to Kafka topic 'user-topic'.")
