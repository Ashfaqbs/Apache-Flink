import json
import time
import threading
from datetime import datetime, timezone
from confluent_kafka import Producer, Consumer

# --- Configuration ---
KAFKA_BROKER = 'localhost:9092' # Outside listener port
INPUT_TOPIC = 'input-topic'
OUTPUT_TOPIC = 'output-topic'

# --- 1. Producer Definition ---
# def run_producer():
#     conf = {'bootstrap.servers': KAFKA_BROKER}
#     producer = Producer(conf)
#
#     print(f"[*] Producer started. Sending data to '{INPUT_TOPIC}'...")
#
#     # We will generate IDs 98 through 103.
#     # Flink should drop 98, 99, 100 and keep 101, 102, 103.
#     for i in range(98, 104):
#         # Generate timestamp in exactly the format Flink expects: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
#         now_utc = datetime.now(timezone.utc)
#         timestamp_str = now_utc.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'
#
#         event = {
#             "id": i,
#             "name": f"EventName-{i}",
#             "timestamp": timestamp_str
#         }
#
#         # Convert dict to JSON string and encode to bytes
#         producer.produce(INPUT_TOPIC, value=json.dumps(event).encode('utf-8'))
#         print(f"[PRODUCER] Sent: {event}")
#
#         # Wait a moment between messages to simulate streaming
#         time.sleep(1)
#
#     producer.flush()
#     print("[*] Producer finished sending messages.")


# --- 1. Producer Definition ---
def run_producer():
    conf = {'bootstrap.servers': KAFKA_BROKER}
    producer = Producer(conf)

    print(f"[*] Producer started. Sending realistic log data to '{INPUT_TOPIC}'...\n")

    # Let's give the AI some real-world scenarios to categorize!
    log_messages = [
        "User 'ashfa' logged in successfully",
        "Disk space on /var/log is at 85%",
        "Payment API connection timeout!",
        "Daily database backup completed",
        "Multiple failed login attempts detected for user 'admin'"
    ]

    for i, log_text in enumerate(log_messages, start=1):
        now_utc = datetime.now(timezone.utc)
        timestamp_str = now_utc.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'

        event = {
            "id": i,
            "name": log_text,
            "timestamp": timestamp_str
        }

        producer.produce(INPUT_TOPIC, value=json.dumps(event).encode('utf-8'))
        print(f"[PRODUCER] Sent: {log_text}")
        time.sleep(15) # Give the local LLM 2 seconds to think between messages

    producer.flush()
    print("\n[*] Producer finished sending messages.")

# --- 2. Consumer Definition ---
def run_consumer():
    conf = {
        'bootstrap.servers': KAFKA_BROKER,
        'group.id': 'python-verification-group',
        'auto.offset.reset': 'earliest' # Read from beginning of topic
    }
    consumer = Consumer(conf)
    consumer.subscribe([OUTPUT_TOPIC])

    print(f"[*] Consumer started. Listening to '{OUTPUT_TOPIC}' for filtered data...\n")

    try:
        while True:
            msg = consumer.poll(timeout=1.0)
            if msg is None:
                continue
            if msg.error():
                print(f"[CONSUMER ERROR] {msg.error()}")
                continue

            # Decode and print the received JSON data
            received_data = json.loads(msg.value().decode('utf-8'))
            print(f">>> [CONSUMER] Received Filtered Event: {received_data}")

    except KeyboardInterrupt:
        print("[*] Consumer interrupted by user.")
    finally:
        consumer.close()

# --- Main Execution ---
if __name__ == '__main__':
    # Start the consumer in a background thread so it's ready to listen
    consumer_thread = threading.Thread(target=run_consumer, daemon=True)
    consumer_thread.start()

    # Give the consumer a second to connect and subscribe
    time.sleep(2)

    # Run the producer in the main thread
    run_producer()

    # Keep the main thread alive for a bit so the consumer can catch the last messages
    print("\n[*] Waiting for final messages to process... (Press Ctrl+C to exit)")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n[*] Exiting.")