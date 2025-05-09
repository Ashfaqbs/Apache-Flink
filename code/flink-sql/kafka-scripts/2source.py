import json
import time
import random
from kafka import KafkaProducer


producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    key_serializer=lambda k: k.encode('utf-8'),
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    acks='all'  
)

def generate_sourceA_data():
    return {
        "id": f"user{random.randint(1, 5)}",
        "valueA": random.randint(10, 100)
    }

def generate_sourceB_data():
    return {
        "id": f"user{random.randint(1, 5)}",
        "valueB": random.randint(100, 1000)
    }

try:
    while True:
        
        data_a = generate_sourceA_data()
        producer.send('source1', key=data_a['id'], value=data_a)
        print(f"Sent to sourceA: {data_a}")

        
        data_b = generate_sourceB_data()
        producer.send('source2', key=data_b['id'], value=data_b)
        print(f"Sent to sourceB: {data_b}")

        
        producer.flush()

        
        time.sleep(2)

except KeyboardInterrupt:
    print("Stopping producer...")

finally:
    producer.close()
