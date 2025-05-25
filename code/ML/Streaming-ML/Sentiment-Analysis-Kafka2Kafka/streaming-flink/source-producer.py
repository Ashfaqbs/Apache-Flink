from kafka  import KafkaProducer
import json
import time

# List of predefined reviews
reviews = [
    "Great product! Loved the quality and build.",
    "Terrible experience. The item broke in 2 days.",
    "Average quality. Not bad, but not excellent.",
    "Excellent! Totally worth the price.",
    "Worst purchase ever. Very disappointed."
]

producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

for review in reviews:
    producer.send('input-topic', review)
    print(f"Sent: {review}")
    time.sleep(1)

producer.flush()
producer.close()
