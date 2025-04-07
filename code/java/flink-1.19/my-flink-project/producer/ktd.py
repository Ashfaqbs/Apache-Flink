from kafka import KafkaProducer
import json
import time
import random
from faker import Faker


fake = Faker()


producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)


for i in range(2100, 2200):  
    record = {
        "id": i,
        "name": fake.name()  
    }

    producer.send('my-topic', record)
    print(f"Sent: {record}")

    time.sleep(1)

producer.flush()

producer.close()