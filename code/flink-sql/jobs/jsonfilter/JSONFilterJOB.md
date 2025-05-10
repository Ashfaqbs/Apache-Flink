- IP :

```

C:\tmp\Apache-Flink\code\flink-sql\jobs\jsonfilter>python prod.py
C:\tmp\Apache-Flink\code\flink-sql\jobs\jsonfilter\prod.py:34: DeprecationWarning: datetime.datetime.utcnow() is deprecated and scheduled for removal in a future version. Use timezone-aware objects to represent datetimes in UTC: datetime.datetime.now(datetime.UTC).
  "event_time": datetime.datetime.utcnow().isoformat() + "Z",
Sent: {"username": "patricia", "userid": "id1", "event_time": "2025-05-10T03:38:11.027240Z", "user_type": "adult", "category": "prime", "orders": [{"orderid": 0, "orderName": "headphones", "qty": 4}]}
Sent: {"username": "james", "userid": "id2", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "adult", "category": "prime", "orders": [{"orderid": 0, "orderName": "hearing aid", "qty": 4}, {"orderid": 1, "orderName": "hearing aid", "qty": 5}]}
Sent: {"username": "steve", "userid": "id3", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "child", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "tablet", "qty": 2}, {"orderid": 1, "orderName": "tablet", "qty": 5}, {"orderid": 2, "orderName": "coloring book", "qty": 1}]}
Sent: {"username": "jessica", "userid": "id4", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "child", "category": "prime", "orders": [{"orderid": 0, "orderName": "laptop", "qty": 2}]}
Sent: {"username": "theresa", "userid": "id5", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "child", "category": "prime", "orders": [{"orderid": 0, "orderName": "walking stick", "qty": 3}, {"orderid": 1, "orderName": "toy car", "qty": 1}, {"orderid": 2, "orderName": "headphones", "qty": 4}]}
Sent: {"username": "ashley", "userid": "id6", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "child", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "hearing aid", "qty": 3}]}
Sent: {"username": "ashley", "userid": "id7", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "adult", "category": "prime", "orders": [{"orderid": 0, "orderName": "coloring book", "qty": 5}, {"orderid": 1, "orderName": "tablet", "qty": 1}]}
Sent: {"username": "dalton", "userid": "id8", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "adult", "category": "prime", "orders": [{"orderid": 0, "orderName": "laptop", "qty": 3}, {"orderid": 1, "orderName": "coloring book", "qty": 3}, {"orderid": 2, "orderName": "hearing aid", "qty": 4}]}
Sent: {"username": "kimberly", "userid": "id9", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "child", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "phone", "qty": 3}]}
Sent: {"username": "bill", "userid": "id10", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "adult", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "toy car", "qty": 5}, {"orderid": 1, "orderName": "hearing aid", "qty": 3}, {"orderid": 2, "orderName": "tablet", "qty": 2}]}
Sent: {"username": "jesse", "userid": "id11", "event_time": "2025-05-10T03:38:11.135693Z", "user_type": "senior", "category": "prime", "orders": [{"orderid": 0, "orderName": "tablet", "qty": 3}]}
Sent: {"username": "francisco", "userid": "id12", "event_time": "2025-05-10T03:38:11.144866Z", "user_type": "adult", "category": "prime", "orders": [{"orderid": 0, "orderName": "phone", "qty": 4}, {"orderid": 1, "orderName": "notebook", "qty": 1}]}
Sent: {"username": "kristine", "userid": "id13", "event_time": "2025-05-10T03:38:11.144866Z", "user_type": "child", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "laptop", "qty": 1}, {"orderid": 1, "orderName": "phone", "qty": 1}]}
Sent: {"username": "hannah", "userid": "id14", "event_time": "2025-05-10T03:38:11.145875Z", "user_type": "child", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "laptop", "qty": 4}]}
Sent: {"username": "renee", "userid": "id15", "event_time": "2025-05-10T03:38:11.146400Z", "user_type": "child", "category": "non-prime", "orders": [{"orderid": 0, "orderNametop", "qty": 5}]}
Sent: {"username": "janet", "userid": "id16", "event_time": "2025-05-10T03:38:11.146400Z", "user_type": "senior", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "headphones", "qty": 1}, {"orderid": 1, "orderName":top", "qty": 5}]}
Sent: {"username": "janet", "userid": "id16", "event_time": "2025-05-10T03:38:11.146400Z", "user_type": "senior", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "headphones", "qty": 1}, {"orderid": 1, "orderName": "hearing aid", "qty": 4}]}
Sent: {"username": "david", "userid": "id17", "event_time": "2025-05-10T03:38:11.147478Z", "user_type": "senior", "category": "prime", "orders": [{"orderid": 0, "orderName": "laptop", "qty": 4}]}
Sent: {"username": "rebecca", "userid": "id18", "event_time": "2025-05-10T03:38:11.147478Z", "user_type": "senior", "category": "prime", "orders": [{"orderid": 0, "orderName": "coloring book", "qty": 3}, {"orderid": 1, "orderName": "coloring book", "qty": 3}]}
Sent: {"username": "chelsea", "userid": "id19", "event_time": "2025-05-10T03:38:11.147478Z", "user_type": "child", "category": "prime", "orders": [{"orderid": 0, "orderName": "walking stick", "qty": 3}, {"orderid": 1, "orderName": "laptop", "qty": 2}, {"orderid": 2, "orderName": "toy car", "qty": 5}]}
Sent: {"username": "marc", "userid": "id20", "event_time": "2025-05-10T03:38:11.147478Z", "user_type": "adult", "category": "non-prime", "orders": [{"orderid": 0, "orderName": "walking stick", "qty": 4}]}


```

- OP : 

```
C:\tmp\Apache-Flink\code\flink-sql\jobs\jsonfilter>python consumer.py
Listening to 'output-topic'...
Received message:
{
  "username": "patricia",
  "userid": "id1",
  "event_time": "2025-05-10T03:38:11.135",
  "user_type": "adult",
  "category": "prime",
  "orders": [
    {
      "orderid": 0,
      "orderName": "headphones",
      "qty": 4
    }
  ]
}
Received message:
{
  "username": "francisco",
  "userid": "id12",
  "event_time": "2025-05-10T03:38:11.144",
  "user_type": "adult",
  "category": "prime",
  "orders": [
    {
      "orderid": 0,
      "orderName": "phone",
      "qty": 4
    },
    {
      "orderid": 1,
      "orderName": "notebook",
      "qty": 1
    }
  ]
}

```

- Flink SQL:

```
Flink SQL> CREATE TABLE kafka_input (
>   username    STRING,
>   userid      STRING,
>   event_time  TIMESTAMP(3) METADATA FROM 'timestamp' VIRTUAL,
>   user_type   STRING,
>   category    STRING,
>   orders      ARRAY<ROW<
>     orderid    INT,
>     orderName  STRING,
>     qty        INT
>   >>
> ) WITH (
>   'connector'                   = 'kafka',
>   'topic'                       = 'Input-topic',
>   'properties.bootstrap.servers'= 'kafka:9093',
>   'properties.group.id'         = 'flink-filter-consumer',
>   'scan.startup.mode'           = 'earliest-offset',
>   'format'                      = 'json',
>   'json.fail-on-missing-field'  = 'false',
>   'json.ignore-parse-errors'    = 'true'
> );
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE kafka_output (
>   username    STRING,
>   userid      STRING,
>   event_time  TIMESTAMP(3),
>   user_type   STRING,
>   category    STRING,
>   orders      ARRAY<ROW<
>     orderid    INT,
>     orderName  STRING,
>     qty        INT
>   >>
> ) WITH (
>   'connector'                   = 'kafka',
>   'topic'                       = 'output-topic',
>   'properties.bootstrap.servers'= 'kafka:9093',
>   'format'                      = 'json',
>   'json.timestamp-format.standard' = 'ISO-8601'
> );
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO kafka_output
> SELECT
>   username,
>   userid,
>   event_time,
>   user_type,
>   category,
>   orders
> FROM kafka_input
> WHERE user_type = 'adult'
>   AND category = 'prime';
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 9b2fa1fe9869a4b2c51bff16e4a858ec

```