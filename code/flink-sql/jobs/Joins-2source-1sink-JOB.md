
- kafka 

```
root@9aa875cda302:/#  kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic source1
Created topic source1.
root@9aa875cda302:/#  kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic source2
Created topic source2.
root@9aa875cda302:/#  kafka-topics.sh --create   --bootstrap-server localhost:9092   --replication-factor 1   --partitions 1   --topic source3
Created topic source3.
root@9aa875cda302:/#


```
- sources 
```
C:\tmp\Apache-Flink\code\flink-sql\kafka-scripts>python 2source.py
Sent to sourceA: {'id': 'user3', 'valueA': 52}
Sent to sourceB: {'id': 'user3', 'valueB': 662}
Sent to sourceA: {'id': 'user5', 'valueA': 75}
Sent to sourceB: {'id': 'user1', 'valueB': 553}
Sent to sourceA: {'id': 'user2', 'valueA': 73}
Sent to sourceB: {'id': 'user3', 'valueB': 862}
Sent to sourceB: {'id': 'user2', 'valueB': 674}
Sent to sourceA: {'id': 'user5', 'valueA': 50}
Sent to sourceB: {'id': 'user1', 'valueB': 186}
Sent to sourceA: {'id': 'user1', 'valueA': 86}
Sent to sourceB: {'id': 'user2', 'valueB': 546}
Sent to sourceA: {'id': 'user3', 'valueA': 20}
Sent to sourceB: {'id': 'user3', 'valueB': 824}
Sent to sourceA: {'id': 'user1', 'valueA': 77}
Sent to sourceB: {'id': 'user4', 'valueB': 322}
Sent to sourceA: {'id': 'user5', 'valueA': 63}
Sent to sourceB: {'id': 'user2', 'valueB': 360}
Sent to sourceA: {'id': 'user5', 'valueA': 20}
Sent to sourceB: {'id': 'user4', 'valueB': 915}
Sent to sourceA: {'id': 'user4', 'valueA': 48}
Sent to sourceB: {'id': 'user1', 'valueB': 270}
Sent to sourceA: {'id': 'user2', 'valueA': 33}
Sent to sourceB: {'id': 'user3', 'valueB': 385}
Sent to sourceA: {'id': 'user2', 'valueA': 30}
Sent to sourceB: {'id': 'user4', 'valueB': 778}
Sent to sourceA: {'id': 'user4', 'valueA': 84}
Sent to sourceB: {'id': 'user5', 'valueB': 199}
Sent to sourceA: {'id': 'user2', 'valueA': 99}
Sent to sourceB: {'id': 'user2', 'valueB': 755}
Sent to sourceA: {'id': 'user4', 'valueA': 86}
Sent to sourceB: {'id': 'user5', 'valueB': 172}
Sent to sourceA: {'id': 'user5', 'valueA': 38}
Sent to sourceB: {'id': 'user3', 'valueB': 706}
Sent to sourceA: {'id': 'user1', 'valueA': 68}
Sent to sourceB: {'id': 'user3', 'valueB': 902}
Sent to sourceA: {'id': 'user3', 'valueA': 55}
Sent to sourceB: {'id': 'user2', 'valueB': 688}
Sent to sourceA: {'id': 'user1', 'valueA': 92}
Sent to sourceB: {'id': 'user3', 'valueB': 428}
Sent to sourceA: {'id': 'user4', 'valueA': 84}
Sent to sourceB: {'id': 'user3', 'valueB': 813}
Sent to sourceA: {'id': 'user5', 'valueA': 11}
Sent to sourceB: {'id': 'user2', 'valueB': 947}
Sent to sourceA: {'id': 'user4', 'valueA': 58}
Sent to sourceB: {'id': 'user2', 'valueB': 955}
Sent to sourceA: {'id': 'user5', 'valueA': 87}
Sent to sourceB: {'id': 'user3', 'valueB': 993}
Sent to sourceA: {'id': 'user5', 'valueA': 20}
Sent to sourceB: {'id': 'user4', 'valueB': 695}
Sent to sourceA: {'id': 'user4', 'valueA': 54}
Sent to sourceB: {'id': 'user1', 'valueB': 347}
Sent to sourceA: {'id': 'user1', 'valueA': 23}
Sent to sourceB: {'id': 'user1', 'valueB': 839}
Sent to sourceA: {'id': 'user2', 'valueA': 35}
Sent to sourceB: {'id': 'user5', 'valueB': 182}
Sent to sourceA: {'id': 'user5', 'valueA': 33}
Sent to sourceB: {'id': 'user1', 'valueB': 977}
Sent to sourceA: {'id': 'user4', 'valueA': 25}
Sent to sourceB: {'id': 'user2', 'valueB': 245}
Sent to sourceA: {'id': 'user5', 'valueA': 92}
Sent to sourceB: {'id': 'user5', 'valueB': 342}
Sent to sourceA: {'id': 'user1', 'valueA': 75}
Sent to sourceB: {'id': 'user3', 'valueB': 492}
Sent to sourceA: {'id': 'user1', 'valueA': 29}
Sent to sourceB: {'id': 'user2', 'valueB': 694}
Sent to sourceA: {'id': 'user5', 'valueA': 68}
Sent to sourceB: {'id': 'user5', 'valueB': 638}
Sent to sourceA: {'id': 'user4', 'valueA': 29}
Sent to sourceB: {'id': 'user5', 'valueB': 537}
Sent to sourceA: {'id': 'user3', 'valueA': 73}
Sent to sourceB: {'id': 'user5', 'valueB': 582}
Sent to sourceA: {'id': 'user4', 'valueA': 26}
Sent to sourceB: {'id': 'user5', 'valueB': 638}
Sent to sourceA: {'id': 'user3', 'valueA': 13}
Sent to sourceB: {'id': 'user1', 'valueB': 573}
Sent to sourceA: {'id': 'user4', 'valueA': 42}
Sent to sourceB: {'id': 'user3', 'valueB': 810}
Sent to sourceA: {'id': 'user4', 'valueA': 77}
Sent to sourceB: {'id': 'user3', 'valueB': 201}
Sent to sourceA: {'id': 'user4', 'valueA': 28}
Sent to sourceB: {'id': 'user5', 'valueB': 241}
Sent to sourceA: {'id': 'user3', 'valueA': 36}
Sent to sourceB: {'id': 'user1', 'valueB': 690}
Sent to sourceA: {'id': 'user2', 'valueA': 69}
Sent to sourceB: {'id': 'user4', 'valueB': 716}
Sent to sourceA: {'id': 'user3', 'valueA': 24}
Sent to sourceB: {'id': 'user4', 'valueB': 725}
Sent to sourceB: {'id': 'user1', 'valueB': 690}
Sent to sourceA: {'id': 'user2', 'valueA': 69}
Sent to sourceB: {'id': 'user4', 'valueB': 716}
Sent to sourceA: {'id': 'user3', 'valueA': 24}
Sent to sourceB: {'id': 'user4', 'valueB': 725}
Sent to sourceA: {'id': 'user2', 'valueA': 69}
Sent to sourceB: {'id': 'user4', 'valueB': 716}
Sent to sourceA: {'id': 'user3', 'valueA': 24}
Sent to sourceB: {'id': 'user4', 'valueB': 725}
Sent to sourceB: {'id': 'user4', 'valueB': 716}
Sent to sourceA: {'id': 'user3', 'valueA': 24}
Sent to sourceB: {'id': 'user4', 'valueB': 725}
Sent to sourceA: {'id': 'user4', 'valueA': 16}
Sent to sourceB: {'id': 'user4', 'valueB': 286}
Sent to sourceA: {'id': 'user2', 'valueA': 96}
Sent to sourceB: {'id': 'user5', 'valueB': 628}
Sent to sourceA: {'id': 'user4', 'valueA': 16}
Sent to sourceB: {'id': 'user4', 'valueB': 286}
Sent to sourceA: {'id': 'user2', 'valueA': 96}
Sent to sourceB: {'id': 'user5', 'valueB': 628}
Sent to sourceA: {'id': 'user2', 'valueA': 45}
Sent to sourceB: {'id': 'user5', 'valueB': 503}
Sent to sourceA: {'id': 'user5', 'valueA': 63}
Sent to sourceA: {'id': 'user2', 'valueA': 96}
Sent to sourceB: {'id': 'user5', 'valueB': 628}
Sent to sourceA: {'id': 'user2', 'valueA': 45}
Sent to sourceB: {'id': 'user5', 'valueB': 503}
Sent to sourceA: {'id': 'user5', 'valueA': 63}
Sent to sourceA: {'id': 'user2', 'valueA': 45}
Sent to sourceB: {'id': 'user5', 'valueB': 503}
Sent to sourceA: {'id': 'user5', 'valueA': 63}
Sent to sourceB: {'id': 'user5', 'valueB': 503}
Sent to sourceA: {'id': 'user5', 'valueA': 63}
Sent to sourceB: {'id': 'user1', 'valueB': 770}
Sent to sourceA: {'id': 'user2', 'valueA': 38}
Sent to sourceB: {'id': 'user4', 'valueB': 636}
Stopping producer...

C:\tmp\Apache-Flink\code\flink-sql\kafka-scripts>
```

- sink 

```

C:\tmp\Apache-Flink\code\flink-sql\kafka-scripts>python consumer.py
 Listening to source3 Topic... Press Ctrl+C to exit.
 Listening to source3 Topic... Press Ctrl+C to exit.

🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 862}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 862}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 674}

 Stopped listening.

C:\tmp\Apache-Flink\code\flink-sql\kafka-scripts>python consumer.py
 Listening to source3 Topic... Press Ctrl+C to exit.

🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 553}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 546}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 862}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 824}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 553}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 360}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 270}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 270}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 915}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 360}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 385}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 360}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 915}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 199}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 360}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 755}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 915}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 199}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 706}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 553}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 270}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 862}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 688}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 688}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 688}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 688}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 553}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 270}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 813}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 915}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 947}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 199}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 955}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 915}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 993}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 199}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 695}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 199}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 347}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 347}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 347}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 347}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 915}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 347}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 553}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 270}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 182}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 360}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 688}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 977}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 977}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 977}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 977}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 977}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 182}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 245}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 915}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 342}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 977}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 347}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 553}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 270}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 492}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 839}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 977}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 347}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 186}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 553}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 270}
🔹 Received message: {'id': 'user2', 'valueA': 35, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 99, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 73, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 33, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 30, 'valueB': 694}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 182}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 537}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 915}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 582}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 862}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 638}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 915}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 573}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 573}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 573}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 573}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 573}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 573}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 573}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 862}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 915}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 810}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 915}
🔹 Received message: {'id': 'user3', 'valueA': 55, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 42, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 20, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 13, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 73, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 52, 'valueB': 201}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 915}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 241}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 36, 'valueB': 862}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 690}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 690}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 690}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 690}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 690}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 690}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 690}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 360}
🔹 Received message: {'id': 'user2', 'valueA': 69, 'valueB': 688}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 716}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 492}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 662}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 201}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 428}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 993}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 902}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 810}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 706}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 824}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 385}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 813}
🔹 Received message: {'id': 'user3', 'valueA': 24, 'valueB': 862}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 716}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 725}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 322}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 695}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 778}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 915}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 286}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 286}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 360}
🔹 Received message: {'id': 'user2', 'valueA': 96, 'valueB': 688}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 628}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 360}
🔹 Received message: {'id': 'user2', 'valueA': 45, 'valueB': 688}
🔹 Received message: {'id': 'user5', 'valueA': 68, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 50, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 20, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 11, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 87, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 75, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 33, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 38, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 92, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 172}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 241}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 638}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 628}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 503}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 582}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 537}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 342}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 199}
🔹 Received message: {'id': 'user5', 'valueA': 63, 'valueB': 182}
🔹 Received message: {'id': 'user1', 'valueA': 77, 'valueB': 770}
🔹 Received message: {'id': 'user1', 'valueA': 29, 'valueB': 770}
🔹 Received message: {'id': 'user1', 'valueA': 68, 'valueB': 770}
🔹 Received message: {'id': 'user1', 'valueA': 86, 'valueB': 770}
🔹 Received message: {'id': 'user1', 'valueA': 75, 'valueB': 770}
🔹 Received message: {'id': 'user1', 'valueA': 92, 'valueB': 770}
🔹 Received message: {'id': 'user1', 'valueA': 23, 'valueB': 770}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 546}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 955}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 694}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 947}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 245}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 674}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 755}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 360}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 688}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 636}
🔹 Received message: {'id': 'user2', 'valueA': 38, 'valueB': 688}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 28, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 84, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 54, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 86, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 16, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 25, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 48, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 26, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 77, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 58, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 636}

 Stopped listening.
🔹 Received message: {'id': 'user4', 'valueA': 29, 'valueB': 636}
🔹 Received message: {'id': 'user4', 'valueA': 42, 'valueB': 636}

 Stopped listening.

 Stopped listening.

C:\tmp\Apache-Flink\code\flink-sql\kafka-scripts>

```

Flink SQL 

```

PS C:\Users\ashfa> docker exec -it docker-jobmanager-1 ./bin/sql-client.sh
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports

                                   ▒▓██▓██▒
                               ▓████▒▒█▓▒▓███▓▒
                            ▓███▓░░        ▒▒▒▓██▒  ▒
                          ░██▒   ▒▒▓▓█▓▓▒░      ▒████
                          ██▒         ░▒▓███▒    ▒█▒█▒
                            ░▓█            ███   ▓░▒██
                              ▓█       ▒▒▒▒▒▓██▓░▒░▓▓█
                            █░ █   ▒▒░       ███▓▓█ ▒█▒▒▒
                            ████░   ▒▓█▓      ██▒▒▒ ▓███▒
                         ░▒█▓▓██       ▓█▒    ▓█▒▓██▓ ░█░
                   ▓░▒▓████▒ ██         ▒█    █▓░▒█▒░▒█▒
                  ███▓░██▓  ▓█           █   █▓ ▒▓█▓▓█▒
                ░██▓  ░█░            █  █▒ ▒█████▓▒ ██▓░▒
               ███░ ░ █░          ▓ ░█ █████▒░░    ░█░▓  ▓░
              ██▓█ ▒▒▓▒          ▓███████▓░       ▒█▒ ▒▓ ▓██▓
           ▒██▓ ▓█ █▓█       ░▒█████▓▓▒░         ██▒▒  █ ▒  ▓█▒
           ▓█▓  ▓█ ██▓ ░▓▓▓▓▓▓▓▒              ▒██▓           ░█▒
           ▓█    █ ▓███▓▒░              ░▓▓▓███▓          ░▒░ ▓█
           ██▓    ██▒    ░▒▓▓███▓▓▓▓▓██████▓▒            ▓███  █
          ▓███▒ ███   ░▓▓▒░░   ░▓████▓░                  ░▒▓▒  █▓
          █▓▒▒▓▓██  ░▒▒░░░▒▒▒▒▓██▓░                            █▓
          ██ ▓░▒█   ▓▓▓▓▒░░  ▒█▓       ▒▓▓██▓    ▓▒          ▒▒▓
          ▓█▓ ▓▒█  █▓░  ░▒▓▓██▒            ░▓█▒   ▒▒▒░▒▒▓█████▒
           ██░ ▓█▒█▒  ▒▓▓▒  ▓█                █░      ░░░░   ░█▒
           ▓█   ▒█▓   ░     █░                ▒█              █▓
            █▓   ██         █░                 ▓▓        ▒█▓▓▓▒█░
             █▓ ░▓██░       ▓▒                  ▓█▓▒░░░▒▓█░    ▒█
              ██   ▓█▓░      ▒                    ░▒█▒██▒      ▓▓
               ▓█▒   ▒█▓▒░                         ▒▒ █▒█▓▒▒░░▒██
                ░██▒    ▒▓▓▒                     ▓██▓▒█▒ ░▓▓▓▓▒█▓
                  ░▓██▒                          ▓░  ▒█▓█  ░░▒▒▒
                      ▒▓▓▓▓▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒░░▓▓  ▓░▒█░

    ______ _ _       _       _____  ____  _         _____ _ _            _  BETA
   |  ____| (_)     | |     / ____|/ __ \| |       / ____| (_)          | |
   | |__  | |_ _ __ | | __ | (___ | |  | | |      | |    | |_  ___ _ __ | |_
   |  __| | | | '_ \| |/ /  \___ \| |  | | |      | |    | | |/ _ \ '_ \| __|
   | |    | | | | | |   <   ____) | |__| | |____  | |____| | |  __/ | | | |_
   |_|    |_|_|_| |_|_|\_\ |_____/ \___\_\______|  \_____|_|_|\___|_| |_|\__|

        Welcome! Enter 'HELP;' to list all available commands. 'QUIT;' to exit.

Command history file path: /root/.flink-sql-history

Flink SQL>  ADD JAR '/opt/flink/custom-lib/flink-sql-connector-kafka-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE sourceA (
>   id STRING,
>   valueA INT
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'source1',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'format' = 'json',
>   'scan.startup.mode' = 'earliest-offset'
> );
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE sourceB (
>   id STRING,
>   valueB INT
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'source2',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'format' = 'json',
>   'scan.startup.mode' = 'earliest-offset'
> );
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE joinedSink (
>   id STRING,
>   valueA INT,
>   valueB INT
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'source3',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'format' = 'json',
>   'sink.delivery-guarantee' = 'at-least-once'
> );
>
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO joinedSink
> SELECT
>   a.id,
>   a.valueA,
>   b.valueB
> FROM sourceA AS a
> JOIN sourceB AS b
> ON a.id = b.id;
>
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 34679784243b42ce3e04b64c89f407e7


```