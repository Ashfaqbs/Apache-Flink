+-------------------+       +-----------------------+
|  Python Script    |       |     Apache Kafka      |
|  (Data Producer)  |======>|     'input-topic'     |
| "Disk space 85%"  |       | {id:2, name: "..."}   |
+-------------------+       +-----------------------+
|
v
=============================================================================
APACHE FLINK (THE ETL & AI ENGINE)
=============================================================================
+--------------------+
| 1. KafkaSource     |  <-- [ EXTRACT ]
| (Deserializes JSON)|
+--------------------+
|
v
+--------------------+      +-----------------------------------+
| 2. processInput()  |----->| Short-Term Memory (Flink State)   |
| (Extracts fields)  |      | Saves: ID=2, Timestamp="...Z"     |
+--------------------+      +-----------------------------------+
|
| (Creates Prompt: "Categorize: Disk space 85%")
v
+--------------------+      +-----------------------------------+
| 3. API Connector   |=====>|    Groq LPU (llama-4-scout)       | <-- [ TRANSFORM ]
| (OpenAI Standard)  |<=====|  (Returns category: "Warning")    |     (Async LLM Call)
+--------------------+      +-----------------------------------+
|
| (Receives "Warning")
v
+--------------------+      +-----------------------------------+
| 4. processChatResp |<-----| Short-Term Memory (Retrieval)     |
| (Recombines Data)  |      | Gets: ID=2, Timestamp="...Z"      |
+--------------------+      +-----------------------------------+
|
| (Creates new enriched string & MyEvent object)
v
+--------------------+
| 5. KafkaSink       |  <-- [ LOAD ]
| (Serializes JSON)  |
+--------------------+
|
v
=============================================================================
|
v
+-----------------------+
|     Apache Kafka      |
|    'output-topic'     |
| {id:2, enriched_name} |
+-----------------------+
|
v
+-------------------+
|  Python Script    |
|  (Data Consumer)  |
| Prints to console |
+-------------------+



### 1. Source: Python Script Mocking

The Python script (`kafka_client.py`) acts as two separate applications running simultaneously:

* **The Producer (Data Source Simulator):** It acts like a live server generating system logs. Every second, it packages a raw log message into a JSON object and publishes it to the Kafka `input-topic`.
* **The Consumer (Downstream Application):** It acts like a monitoring dashboard. It constantly listens to the Kafka `output-topic` and prints out the final, AI-enriched JSON objects as soon as Flink is done processing them.

### 2. The Kafka Events (The Data Payload)

The "events" being sent into Kafka are simple JSON objects that map perfectly to our Java `MyEvent` class.

Here is what the Producer sends into the `input-topic`:

```json
{
  "id": 2,
  "name": "Disk space on /var/log is at 85%",
  "timestamp": "2026-02-22T07:38:53.244Z"
}

```

* **`id`:** A unique integer tracking the message.
* **`name`:** The raw text of the system log.
* **`timestamp`:** An ISO-8601 formatted string representing exactly when the log was generated.

### 3. The Flink Agent Events (The Internal Processing)

Once that JSON hits Flink, the Flink Agent framework uses its own internal "Event" system to orchestrate the AI logic. Here is the sequence:

* **`InputEvent`:** The Flink Agent intercepts the Kafka JSON. It saves the `id` and `timestamp` into memory.
* **`ChatRequestEvent`:** The Flink Agent takes the `name` (the log text), wraps it in our custom prompt, and fires an asynchronous HTTP request to the Groq LLM.
* **`ChatResponseEvent`:** Groq replies with the category (e.g., "Warning"). The Flink Agent catches this, pulls the original `id` and `timestamp` out of memory, and creates a newly enriched string (e.g., `Disk space... [AI Category: Warning]`).
* **`OutputEvent`:** The Flink Agent packages this enriched data back into a `MyEvent` POJO and sends it to the `KafkaSink`, which drops it into the `output-topic` for our Python Consumer to read.

---