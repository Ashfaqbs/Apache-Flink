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