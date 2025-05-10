from kafka import KafkaConsumer
import json

# Kafka consumer setup
consumer = KafkaConsumer(
    'output-topic',
    bootstrap_servers='localhost:9092',
    auto_offset_reset='earliest',        # 'latest' to skip old messages
    enable_auto_commit=True,
    group_id='my-consumer-group1',
    value_deserializer=lambda x: json.loads(x.decode('utf-8'))
)

print("Listening to 'output-topic'...")

for message in consumer:
    print("Received message:")
    print(json.dumps(message.value, indent=2))
