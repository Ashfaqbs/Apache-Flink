from kafka import KafkaConsumer

consumer = KafkaConsumer(
    'output-topic',
    bootstrap_servers=['localhost:9092'],
    auto_offset_reset='earliest',
    enable_auto_commit=True,
    value_deserializer=lambda m: m.decode('utf-8')
)

print("Listening for messages on 'output-topic'...")

for message in consumer:
    print(f"Received: {message.value}")
