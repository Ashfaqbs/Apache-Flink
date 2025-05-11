from kafka import KafkaProducer
import json
import random
from datetime import datetime
import time

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

known_items = ["Washer", "Fridge", "Microwave", "Dishwasher", "Oven", "Blender", "TV", "Refrigerator", "Toaster", "Coffee Maker"]

def generate_random_orders():
    orders = []
    total_items = 0

    while total_items < 10:
        num_items = random.randint(1, 5) 
        total_items += num_items

        for _ in range(num_items):
            item_name = random.choice(known_items)
            item_qty = random.randint(1, 5)  
            cvar_value = f"randomValue{random.randint(1, 100)}"
            
            order = {
                "itemId": str(random.randint(100, 999)),
                "Itemname": item_name,
                "ItemQty": item_qty,
                "cJSON": {"cVar": cvar_value}
            }
            orders.append(order)

    return orders

def send_order_to_kafka(email, orders, topic='topic-1'):
    message = {
        "cEmail": email,
        "CTimeStamp": datetime.utcnow().isoformat() + "Z",
        "orders": orders
    }
    producer.send(topic, message)
    producer.flush()
    print(f"\n Sent message to {topic}:\n{json.dumps(message, indent=2)}")


if __name__ == "__main__":
    user_email = "user@example.com"

    while True:
        orders_list = generate_random_orders()

        send_order_to_kafka(user_email, orders_list)

        print("\n Waiting for next batch (5 minutes)...")
        time.sleep(300)  # 300 seconds = 5 minutes
