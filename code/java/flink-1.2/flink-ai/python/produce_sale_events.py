"""
Producer for InventoryReorderJob
==================================
Sends SaleEvent events to Kafka topic: sale-events
Each message is consumed by SalesVelocityAgent (Agent 1) which classifies
velocity and estimates days-to-stockout. After the 5-minute window closes,
ReorderAgent (Agent 2) generates urgency-ranked reorder recommendations
and calls notifyProcurement / sendRestockAlert for CRITICAL/HIGH urgency.

Schema:
{
  "skuId":         str,
  "productName":   str,
  "quantitySold":  int,
  "currentStock":  int,
  "warehouseId":   str,
  "timestamp":     str  (ISO-8601)
}

Multiple events with the same skuId simulate real sales velocity within a window.
The window aggregates them before Agent 2 produces a recommendation.

Tip: Reduce window to 30 seconds for demo by changing Duration.ofMinutes(5)
     in InventoryReorderJob.java.

Run:
    pip install confluent-kafka
    python python/produce_sale_events.py
"""

import json
import time
from datetime import datetime, timezone
from confluent_kafka import Producer

KAFKA_BROKER = "localhost:9092"
TOPIC = "sale-events"

# Each entry is one sale event. Multiple events for the same SKU simulate
# real streaming sales data that the window will aggregate.
# currentStock decreases across repeated events to simulate depletion.
SAMPLE_SALE_EVENTS = [
    # --- LOW urgency: healthy stock, slow mover ---
    {
        "skuId": "SKU-LOW-001",
        "productName": "Office Chair Armrest Pad",
        "quantitySold": 2,
        "currentStock": 540,
        "warehouseId": "WH-West",
        "expected_urgency": "LOW",
    },
    {
        "skuId": "SKU-LOW-001",
        "productName": "Office Chair Armrest Pad",
        "quantitySold": 1,
        "currentStock": 538,
        "warehouseId": "WH-West",
        "expected_urgency": "LOW",
    },
    # --- MEDIUM urgency: moderate velocity, some concern ---
    {
        "skuId": "SKU-MED-001",
        "productName": "Portable Phone Charger 20000mAh",
        "quantitySold": 15,
        "currentStock": 180,
        "warehouseId": "WH-Central",
        "expected_urgency": "MEDIUM",
    },
    {
        "skuId": "SKU-MED-001",
        "productName": "Portable Phone Charger 20000mAh",
        "quantitySold": 22,
        "currentStock": 158,
        "warehouseId": "WH-Central",
        "expected_urgency": "MEDIUM",
    },
    # --- HIGH urgency: fast depletion, below safety stock ---
    # notifyProcurement and sendRestockAlert tools should fire
    {
        "skuId": "SKU-HIGH-001",
        "productName": "Wireless Noise-Cancelling Headphones",
        "quantitySold": 45,
        "currentStock": 95,
        "warehouseId": "WH-East",
        "expected_urgency": "HIGH",
    },
    {
        "skuId": "SKU-HIGH-001",
        "productName": "Wireless Noise-Cancelling Headphones",
        "quantitySold": 52,
        "currentStock": 43,
        "warehouseId": "WH-East",
        "expected_urgency": "HIGH",
    },
    # --- CRITICAL urgency: near stockout, very high velocity ---
    # Both notifyProcurement and sendRestockAlert tools must fire
    {
        "skuId": "SKU-CRIT-001",
        "productName": "USB-C Hub 7-in-1",
        "quantitySold": 80,
        "currentStock": 40,
        "warehouseId": "WH-North",
        "expected_urgency": "CRITICAL",
    },
    {
        "skuId": "SKU-CRIT-001",
        "productName": "USB-C Hub 7-in-1",
        "quantitySold": 35,
        "currentStock": 5,
        "warehouseId": "WH-North",
        "expected_urgency": "CRITICAL",
    },
    # --- CRITICAL: viral product, stock didn't anticipate the spike ---
    {
        "skuId": "SKU-CRIT-002",
        "productName": "Retro Mechanical Keyboard",
        "quantitySold": 120,
        "currentStock": 28,
        "warehouseId": "WH-South",
        "expected_urgency": "CRITICAL",
    },
]


def make_event(profile: dict) -> dict:
    """Builds a SaleEvent payload from a profile template."""
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z"
    return {
        "skuId": profile["skuId"],
        "productName": profile["productName"],
        "quantitySold": profile["quantitySold"],
        "currentStock": profile["currentStock"],
        "warehouseId": profile["warehouseId"],
        "timestamp": now,
    }


def delivery_report(err, msg):
    if err:
        print(f"  [ERROR] Delivery failed: {err}")
    else:
        print(f"  [OK]    Delivered to {msg.topic()} partition {msg.partition()}")


def main():
    producer = Producer({"bootstrap.servers": KAFKA_BROKER})
    print(f"[*] Sending sale events to '{TOPIC}' on {KAFKA_BROKER}")
    print(f"[*] Covers LOW / MEDIUM / HIGH / CRITICAL reorder urgency scenarios")
    print(f"[*] Results appear after the 5-minute window closes\n")

    total = len(SAMPLE_SALE_EVENTS)
    for i, profile in enumerate(SAMPLE_SALE_EVENTS):
        event = make_event(profile)
        stock_pct = round(profile["currentStock"] / (profile["currentStock"] + profile["quantitySold"]) * 100)
        print(f"[{i+1:02d}/{total}] Sending | Expected: {profile['expected_urgency']:8s} | "
              f"SKU: {profile['skuId']:<16s} | "
              f"Sold: {profile['quantitySold']:>3d} | "
              f"Stock: {profile['currentStock']:>4d} | "
              f"Warehouse: {profile['warehouseId']}")

        producer.produce(
            TOPIC,
            key=profile["skuId"].encode("utf-8"),   # key by skuId — window groups by this
            value=json.dumps(event).encode("utf-8"),
            callback=delivery_report,
        )
        producer.poll(0)

        time.sleep(3)

    producer.flush()
    print(f"\n[*] Done. {total} sale events sent to '{TOPIC}'.")
    print("[*] Wait for window to close, then monitor reorder-recommendations topic.")
    print("[*] CRITICAL/HIGH SKUs should show tool calls in Flink TaskManager logs.")


if __name__ == "__main__":
    main()
