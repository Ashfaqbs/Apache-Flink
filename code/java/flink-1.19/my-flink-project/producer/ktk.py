from kafka import KafkaProducer
import json
import time
from faker import Faker
from datetime import datetime, timezone  

fake = Faker()

producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8')
)

for i in range(2100, 2200):
    record = {
        "id": i,
        "name": fake.name(),
        "timestamp": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"
    }
    producer.send('input-topic', record)
    print(f"Sent: {record}")
    time.sleep(1)

producer.flush()
producer.close()
