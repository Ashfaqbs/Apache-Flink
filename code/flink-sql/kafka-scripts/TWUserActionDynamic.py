from kafka import KafkaProducer
import json
from datetime import datetime

from time import sleep

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

usernames = ['Mary', 'Bob', 'Liz']
page_urls = ['./home', './cart', './prod?id=1', './prod?id=2']

for i in range(20):
    event = {
        "username": usernames[i % 3],
        "event_time": datetime.utcnow().isoformat(),  # 🔥 live timestamp
        "page_url": page_urls[i % 4]
    }
    print("Sending:", event)
    producer.send('user-topic', value=event)
    sleep(1)  # Send one every second

producer.flush()
print("Done sending live events.")
