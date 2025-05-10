import json
import random
import datetime
from kafka import KafkaProducer
from faker import Faker

fake = Faker()

# Kafka producer setup
producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Random choices
user_types = ["adult", "child", "senior"]
categories = ["prime", "non-prime"]
product_catalog = [
    "phone", "laptop", "toy car", "coloring book", 
    "hearing aid", "walking stick", "notebook", "headphones", "tablet"
]

def generate_order(order_id):
    return {
        "orderid": order_id,
        "orderName": random.choice(product_catalog),
        "qty": random.randint(1, 5)
    }

def generate_user(userid):
    return {
        "username": fake.first_name().lower(),
        "userid": userid,
        "event_time": datetime.datetime.utcnow().isoformat() + "Z",
        "user_type": random.choice(user_types),
        "category": random.choice(categories),
        "orders": [generate_order(i) for i in range(random.randint(1, 3))]
    }

# Send 20 messages
for i in range(10):
    message = generate_user(f"id{i+1}")
    producer.send("Input-topic", message)
    print(f"Sent: {json.dumps(message)}")

producer.flush()
print("All messages sent.")
