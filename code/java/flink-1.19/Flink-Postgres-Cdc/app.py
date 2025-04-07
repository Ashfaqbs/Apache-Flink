import psycopg2
from datetime import datetime
import random

# Database connection config
conn = psycopg2.connect(
    host="localhost",       # or the name of your docker postgres service, like 'postgres'
    port=5432,
    database="streamdb",
    user="postgres",
    password="postgres"
)

cur = conn.cursor()

# Insert 10 rows
for i in range(10, 20):
    cur.execute("""
        INSERT INTO labschema.stream_note (id, added_date, content, is_live, title)
        VALUES (%s, %s, %s, %s, %s)
    """, (
        i,
        datetime.now(),
        f"CDC Note content {i}",
        random.choice([True, False]),
        f"Note Title {i}"
    ))

conn.commit()
print("Inserted 10 rows into stream_note table.")

cur.close()
conn.close()


"""
Task manager logs:

2025-04-07 16:03:47,803 INFO  io.debezium.connector.postgresql.PostgresStreamingChangeEventSource [] - Processing messages
2025-04-07 16:03:47,812 INFO  io.debezium.connector.postgresql.connection.WalPositionLocator [] - Message with LSN 'LSN{0/1A529A0}' arrived, switching off the filtering
2025-04-07 16:03:48,270 INFO  io.debezium.connector.common.BaseSourceTask                  [] - 5 records sent during previous 00:00:45.628, last recorded offset: {transaction_id=null, lsn_proc=27601312, lsn=27601312, txId=749, ts_usec=1744041827355624}
+I[102, 2025-04-07T16:03:47.355055, CDC Test Note, true, Testing Flink CDC]
2025-04-07 16:08:03,279 INFO  io.debezium.connector.common.BaseSourceTask                  [] - 1 records sent during previous 00:04:15.009, last recorded offset: {transaction_id=null, lsn_proc=27602144, lsn_commit=27602144, lsn=27602144, txId=749, ts_usec=1744041827355624}
2025-04-07 16:13:02,877 INFO  io.debezium.connector.common.BaseSourceTask                  [] - 1 records sent during previous 00:04:59.598, last recorded offset: {transaction_id=null, lsn_proc=27602144, lsn_commit=27602144, lsn=27602144, txId=749, ts_usec=1744041827355624}
2025-04-07 16:18:02,977 INFO  io.debezium.connector.common.BaseSourceTask                  [] - 1 records sent during previous 00:05:00.1, last recorded offset: {transaction_id=null, lsn_proc=27602144, lsn_commit=27602144, lsn=27602144, txId=749, ts_usec=1744041827355624}
2025-04-07 16:26:19,467 INFO  io.debezium.connector.common.BaseSourceTask                  [] - 2 records sent during previous 00:08:16.49, last recorded offset: {transaction_id=null, lsn_proc=27602432, lsn_commit=27602144, lsn=27602432, txId=750, ts_usec=1744043178797278}
+I[103, 2025-04-07T16:26:18.796702, CDC Test Note, true, Testing Flink CDC]
+I[104, 2025-04-07T16:27:01.959842, CDC Test Note, true, Testing Flink CDC]
+I[10, 2025-04-07T22:10:00.752459, CDC Note content 10, true, Note Title 10]
+I[11, 2025-04-07T22:10:00.760463, CDC Note content 11, false, Note Title 11]
+I[12, 2025-04-07T22:10:00.760463, CDC Note content 12, false, Note Title 12]
+I[13, 2025-04-07T22:10:00.761453, CDC Note content 13, true, Note Title 13]
+I[14, 2025-04-07T22:10:00.761453, CDC Note content 14, false, Note Title 14]
+I[15, 2025-04-07T22:10:00.762453, CDC Note content 15, false, Note Title 15]
+I[16, 2025-04-07T22:10:00.762453, CDC Note content 16, false, Note Title 16]
+I[17, 2025-04-07T22:10:00.763452, CDC Note content 17, true, Note Title 17]
+I[18, 2025-04-07T22:10:00.764453, CDC Note content 18, true, Note Title 18]
+I[19, 2025-04-07T22:10:00.764453, CDC Note content 19, true, Note Title 19] 

"""