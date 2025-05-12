- Event Time NW

K -> K flow

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

Flink SQL> CREATE TABLE user_orders_2 (
>   cEmail       STRING,
>   CTimeStamp   STRING,
>   orders       ARRAY<ROW<
>                   itemId    STRING,
>                   Itemname  STRING,
>                   ItemQty   INT,
>                   cJSON     ROW<cVar STRING>
>                 >>,
>   event_time AS TO_TIMESTAMP(CTimeStamp, 'yyyy-MM-dd''T''HH:mm:ss''Z'''),
>   WATERMARK FOR event_time AS event_time - INTERVAL '5' MINUTES
> ) WITH (
>   'connector'                   = 'kafka',
>   'topic'                       = 'topic-1',
>   'properties.bootstrap.servers'= 'kafka:9093',
>   'format'                      = 'json',
>   'scan.startup.mode'           = 'earliest-offset'
> );
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE order_counts_2 (
>   window_start   STRING,
>    window_end     STRING,
>    Itemname       STRING,
>    totalQty       BIGINT
> ) WITH (
>    'connector'                   = 'kafka',
>    'topic'                       = 'topic-2',
>    'properties.bootstrap.servers'= 'kafka:9093',
>    'format'                      = 'json',
>    'sink.delivery-guarantee'     = 'at-least-once'
>  );
[INFO] Execute statement succeed.



Flink SQL> INSERT INTO order_counts_2
> SELECT
>   DATE_FORMAT(TUMBLE_START(event_time, INTERVAL '5' MINUTE), 'yyyy-MM-dd HH:mm:ss') AS window_start,
>   DATE_FORMAT(TUMBLE_END(event_time, INTERVAL '5' MINUTE), 'yyyy-MM-dd HH:mm:ss') AS window_end,
>   T.Itemname,
>   SUM(T.ItemQty) AS totalQty
> FROM user_orders_2
>   CROSS JOIN UNNEST(orders) AS T(itemId, Itemname, ItemQty, cJSON)
> GROUP BY
>   T.Itemname,
>   TUMBLE(event_time, INTERVAL '5' MINUTE);
[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.table.api.ValidationException: Could not find any factory for identifier 'kafka' that implements 'org.apache.flink.table.factories.DynamicTableFactory' in the classpath.

Available factory identifiers are:

blackhole
datagen
filesystem
print
python-input-format

Flink SQL> ADD JAR '/opt/flink/custom-lib/flink-sql-connector-kafka-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO order_counts_2
> SELECT
>   DATE_FORMAT(TUMBLE_START(event_time, INTERVAL '5' MINUTE), 'yyyy-MM-dd HH:mm:ss') AS window_start,
>   DATE_FORMAT(TUMBLE_END(event_time, INTERVAL '5' MINUTE), 'yyyy-MM-dd HH:mm:ss') AS window_end,
>   T.Itemname,
>   SUM(T.ItemQty) AS totalQty
> FROM user_orders_2
>   CROSS JOIN UNNEST(orders) AS T(itemId, Itemname, ItemQty, cJSON)
> GROUP BY
>   T.Itemname,
>   TUMBLE(event_time, INTERVAL '5' MINUTE);
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: df7039508300205478f9d00c8b206f76



```


k -> k flow 2 NW

```
Flink SQL> CREATE TABLE user_orders (
>   cEmail     STRING,
>   CTimeStamp STRING,
>   orders     ARRAY<ROW<
>                 itemId    STRING,
>                 Itemname  STRING,
>                 ItemQty   INT,
>                 cJSON     ROW<cVar STRING>
>               >>,
>   event_ts AS CAST(CTimeStamp AS TIMESTAMP(3)),
>   WATERMARK FOR event_ts AS event_ts - INTERVAL '1' MINUTE
> ) WITH (
>   'connector'                    = 'kafka',
>   'topic'                        = 'sourceb',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'properties.group.id'          = 'flink-eventtime-consumer',
>   'scan.startup.mode'            = 'earliest-offset',
>   'format'                       = 'json',
>   'json.fail-on-missing-field'   = 'false',
>   'json.ignore-parse-errors'     = 'true'
> );
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE orders_agg (
>   window_start TIMESTAMP(3),
>   window_end   TIMESTAMP(3),
>   Itemname     STRING,
>   total_qty    BIGINT
> ) WITH (
>   'connector'                    = 'kafka',
>   'topic'                        = 'sourcec',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'format'                       = 'json',
>   'json.timestamp-format.standard' = 'ISO-8601'
> );
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO orders_agg
> SELECT
>   TUMBLE_START(event_ts, INTERVAL '5' MINUTE) AS window_start,
>   TUMBLE_END(event_ts,   INTERVAL '5' MINUTE) AS window_end,
>   item.Itemname                         AS Itemname,
>   SUM(item.ItemQty)                     AS total_qty
> FROM user_orders
>   CROSS JOIN UNNEST(orders) AS item (itemId, Itemname, ItemQty, cJSON)
> GROUP BY
>   TUMBLE(event_ts, INTERVAL '5' MINUTE),
>   item.Itemname;
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: f5e2f044f86c0fbbfe4fe7f057906955

```



K -> DB NW





-------------------Source 

Flink SQL> CREATE TABLE user_orders (
   cEmail      STRING,
   CTimeStamp  TIMESTAMP_LTZ(3),                           
   orders      ARRAY<ROW<
                  itemId    STRING,
                  Itemname  STRING,
                  ItemQty   INT,
                  cJSON     ROW<cVar STRING>
                >>,
   WATERMARK FOR CTimeStamp AS CTimeStamp - INTERVAL '1' MINUTE
 ) WITH (
   'connector'                         = 'kafka',
   'topic'                             = 'topic-2',
  'properties.bootstrap.servers'      = 'kafka:9093',
   'properties.group.id'               = 'flink-eventtime-consumer',
   'scan.startup.mode'                 = 'earliest-offset',
   'format'                            = 'json',
   'json.fail-on-missing-field'        = 'false',
   'json.ignore-parse-errors'          = 'true',
   'json.timestamp-format.standard'    = 'ISO-8601'     
  );



OR 
-----------------------------   -- zero lag----------------------------



CREATE TABLE user_orders (
   cEmail      STRING,
   CTimeStamp  TIMESTAMP_LTZ(3),                           
   orders      ARRAY<ROW<
                  itemId    STRING,
                  Itemname  STRING,
                  ItemQty   INT,
                  cJSON     ROW<cVar STRING>
                >>,
    WATERMARK FOR CTimeStamp AS CTimeStamp                   
 ) WITH (
   'connector'                         = 'kafka',
   'topic'                             = 'topic-2',
  'properties.bootstrap.servers'      = 'kafka:9093',
   'properties.group.id'               = 'flink-eventtime-consumer',
   'scan.startup.mode'                 = 'earliest-offset',
   'format'                            = 'json',
   'json.fail-on-missing-field'        = 'false',
   'json.ignore-parse-errors'          = 'true',
   'json.timestamp-format.standard'    = 'ISO-8601'     
  );




-------------------------------------------------------------------------SINK 


CREATE TABLE order_counts (
   window_start TIMESTAMP(3),
   window_end   TIMESTAMP(3),
   itemname     STRING,
   totalqty     BIGINT,
   PRIMARY KEY (window_start, itemname) NOT ENFORCED
 ) WITH (
   'connector'                  = 'jdbc',
   'url'                        = 'jdbc:postgresql://postgres:5432/mainschema',
   'table-name'                 = 'order_counts',
   'username'                   = 'postgres',
   'password'                   = 'admin',
   'driver'                     = 'org.postgresql.Driver',
   'sink.buffer-flush.max-rows'= '500',
   'sink.buffer-flush.interval'= '30s'
 );





--------DML 


INSERT INTO order_counts
 SELECT
   CAST(TUMBLE_START(CTimeStamp, INTERVAL '5' MINUTE) AS TIMESTAMP(3)),
   CAST(TUMBLE_END(CTimeStamp,   INTERVAL '5' MINUTE) AS TIMESTAMP(3)),
   item.Itemname,
   SUM(item.ItemQty)
 FROM user_orders
   CROSS JOIN UNNEST(orders) AS item(itemId, Itemname, ItemQty, cJSON)
 GROUP BY
   TUMBLE(CTimeStamp, INTERVAL '5' MINUTE),
   item.Itemname;

[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: fa00ac7dfba7dbf26cb12b486d5b03df