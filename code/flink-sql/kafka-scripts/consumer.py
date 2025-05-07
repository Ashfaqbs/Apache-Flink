from kafka import KafkaConsumer
import json

consumer = KafkaConsumer(
    'user-topic',
    bootstrap_servers='localhost:9092',
    auto_offset_reset='earliest',
    enable_auto_commit=True,
    group_id='n-topic-group1',
    value_deserializer=lambda m: json.loads(m.decode('utf-8'))
)

print(" Listening to 'user-topic'... Press Ctrl+C to exit.\n")

try:
    for message in consumer:
        print(f"🔹 Received message: {message.value}")
except KeyboardInterrupt:
    print("\n Stopped listening.")
