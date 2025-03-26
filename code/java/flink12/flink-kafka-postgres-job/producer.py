from kafka import KafkaProducer
import json
import time
import random
from faker import Faker

# Initialize Faker for generating random names
fake = Faker()

# Create Kafka producer instance
producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Loop to generate and send data from 100 to 200
for i in range(100, 201):  # Loop from 100 to 200 (inclusive)
    record = {
        "id": i,
        "name": fake.name()  # Generate a random name using Faker
    }
    
    # Send data to Kafka
    producer.send('my-topic', record)
    print(f"Sent: {record}")
    
    # Optional: Add a delay (e.g., 1 second) between sending messages
    time.sleep(1)

# Ensure all messages are sent before closing
producer.flush()

# Close the producer connection
producer.close()
