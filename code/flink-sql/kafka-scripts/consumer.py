from kafka import KafkaConsumer
import json

topic = 'sourcec'
consumer = KafkaConsumer(
    topic,
    bootstrap_servers='localhost:9092',
    auto_offset_reset='earliest',
    enable_auto_commit=True,
    group_id='n-topic-group',
    value_deserializer=lambda m: json.loads(m.decode('utf-8'))
)

print(f" Listening to {topic} Topic... Press Ctrl+C to exit.\n")

try:
    for message in consumer:
        print(f"🔹 Received message: {message.value}")
except KeyboardInterrupt:
    print("\n Stopped listening.")
