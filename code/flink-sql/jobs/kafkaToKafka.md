
Producer

C:\tmp\flink-sql\kafka-scripts>python producer.py
Sending: {'name': 'Sheila Grant', 'email': 'sylvia83@example.net', 'role': 'developer'}
Sending: {'name': 'Crystal Osborne', 'email': 'travis02@example.net', 'role': 'architect'}
Sending: {'name': 'Cody Payne', 'email': 'renee41@example.org', 'role': 'analyst'}
Sending: {'name': 'Robyn Morrow', 'email': 'katelynjackson@example.net', 'role': 'developer'}
Sending: {'name': 'Ashley Barnett', 'email': 'shannonsmith@example.com', 'role': 'architect'}
Sending: {'name': 'Tammy Brown', 'email': 'hilldevin@example.net', 'role': 'tester'}
Sending: {'name': 'Jill Johnson', 'email': 'michaelwilliams@example.org', 'role': 'manager'}
Sending: {'name': 'Alexandra Preston', 'email': 'marthaprice@example.org', 'role': 'tester'}
Sending: {'name': 'Michelle Burns PhD', 'email': 'rogerskatrina@example.com', 'role': 'analyst'}
Sending: {'name': 'Kayla Robinson', 'email': 'amygreer@example.net', 'role': 'developer'}
✅ All messages sent.

C:\tmp\flink-sql\kafka-scripts> 


Consumer 

C:\tmp\flink-sql\kafka-scripts>python consumer.py
✅ Listening to 'o-topic'... Press Ctrl+C to exit.

🔹 Received message: {'name': 'SHEILA GRANT', 'email': 'sylvia83@example.net', 'role': 'developer'}
🔹 Received message: {'name': 'ROBYN MORROW', 'email': 'katelynjackson@example.net', 'role': 'developer'}
🔹 Received message: {'name': 'KAYLA ROBINSON', 'email': 'amygreer@example.net', 'role': 'developer'}




Kafka 

PS C:\Users\ashfa> docker exec -it docker-kafka-1 kafka-topics.sh --create  --topic i-topic  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
Created topic i-topic.

What's next:
    Try Docker Debug for seamless, persistent debugging tools in any container or image → docker debug docker-kafka-1
    Learn more at https://docs.docker.com/go/debug-cli/
PS C:\Users\ashfa> docker exec -it docker-kafka-1 kafka-topics.sh --create  --topic o-topic  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
Created topic o-topic.



Flink :



Flink SQL> CREATE TABLE kafka_input (
>   name STRING,
>   email STRING,
>   role STRING
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'i-topic',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'properties.group.id' = 'flink-i-consumer',
>   'scan.startup.mode' = 'earliest-offset',
>   'format' = 'json',
>   'json.ignore-parse-errors' = 'true'
> );
>
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE kafka_output (
>   name STRING,
>   email STRING,
>   role STRING
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'o-topic',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'format' = 'json'
> );
>
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO kafka_output
> SELECT
>   UPPER(name) AS name,
>   email,
>   role
> FROM kafka_input
> WHERE role = 'developer';
>
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 2d034349fb5fa2af873658e7abdb0579


Flink SQL>


Flink UI 

![alt text](/jobs/images/k2k.png)