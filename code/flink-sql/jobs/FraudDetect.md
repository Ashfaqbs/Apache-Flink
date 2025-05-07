
Docker 

```
PS C:\Users\ashfa> docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED      STATUS       PORTS                                                NAMES
687f1346b7d1   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   6 days ago   Up 2 hours   6123/tcp, 8081/tcp                                   docker-taskmanager-2
22876acb6aaa   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   6 days ago   Up 2 hours   6123/tcp, 8081/tcp                                   docker-taskmanager-1
ead02749ca74   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   6 days ago   Up 2 hours   6123/tcp, 0.0.0.0:8081->8081/tcp                     docker-jobmanager-1
c3c198a8abd8   wurstmeister/zookeeper:latest   "/bin/sh -c '/usr/sb…"   6 days ago   Up 2 hours   22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   docker-zookeeper-1
cac11f27e2d9   postgres:latest                 "docker-entrypoint.s…"   6 days ago   Up 3 hours   0.0.0.0:5432->5432/tcp                               docker-postgres-1
e970f9618fa2   wurstmeister/kafka:latest       "start-kafka.sh"         6 days ago   Up 2 hours   0.0.0.0:9092->9092/tcp, 9093/tcp                     docker-kafka-1
384a39d7fa4c   mongo:latest                    "docker-entrypoint.s…"   6 days ago   Up 2 hours   0.0.0.0:27017->27017/tcp                             docker-mongo-1
PS C:\Users\ashfa>
```

Kafka 

```
PS C:\Users\ashfa> docker exec -it docker-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
my-topic

What's next:
    Try Docker Debug for seamless, persistent debugging tools in any container or image → docker debug docker-kafka-1
    Learn more at https://docs.docker.com/go/debug-cli/
PS C:\Users\ashfa> docker exec -it docker-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create  --topic bank-transactions --partitions 3   --replication-factor 1
Created topic bank-transactions.

What's next:
    Try Docker Debug for seamless, persistent debugging tools in any container or image → docker debug docker-kafka-1
    Learn more at https://docs.docker.com/go/debug-cli/
PS C:\Users\ashfa> docker exec -it docker-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create  --topic fraud-mv-topic --partitions 3   --replication-factor 1
Error while executing topic command : Topic 'fraud-mv-topic' already exists.
[2025-05-06 17:10:05,549] ERROR org.apache.kafka.common.errors.TopicExistsException: Topic 'fraud-mv-topic' already exists.
 (kafka.admin.TopicCommand$)

What's next:
    Try Docker Debug for seamless, persistent debugging tools in any container or image → docker debug docker-kafka-1
    Learn more at https://docs.docker.com/go/debug-cli/
PS C:\Users\ashfa>
```


Flink 

````

Loading personal and system profiles took 3560ms.
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

Flink SQL> show jars;
Empty set

Flink SQL> ADD JAR '/opt/flink/custom-lib/flink-sql-connector-kafka-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL> ADD JAR '/opt/flink/custom-lib/flink-connector-jdbc-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL> ADD JAR '/opt/flink/custom-lib/postgresql-42.6.0.jar';
[INFO] Execute statement succeed.

Flink SQL> show jars;
+----------------------------------------------------------------+
|                                                           jars |
+----------------------------------------------------------------+
|                    /opt/flink/custom-lib/postgresql-42.6.0.jar |
|      /opt/flink/custom-lib/flink-connector-jdbc-3.3.0-1.19.jar |
| /opt/flink/custom-lib/flink-sql-connector-kafka-3.3.0-1.19.jar |
+----------------------------------------------------------------+
3 rows in set

Flink SQL> CREATE TABLE kafka_bank_txn (
>   transactionId STRING,
>   timestamp TIMESTAMP(3),
>   accountId STRING,
>   sourceAccount STRING,
>   destinationAccount STRING,
>   amount DOUBLE,
>   currency STRING,
>   transactionType STRING,
>   channel STRING,
>   location ROW<ip STRING, city STRING, country STRING>,
>   status STRING,
>   fraudFlag BOOLEAN,
>   fraudReason STRING,
>   fraudScore DOUBLE,
>   verdict STRING
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'bank-transactions',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'properties.group.id' = 'fraud-filter-group',
>   'scan.startup.mode' = 'earliest-offset',
>   'format' = 'json',
>   'json.ignore-parse-errors' = 'true'
> );
>
[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.sql.parser.impl.ParseException: Encountered "timestamp" at line 3, column 3.
Was expecting one of:
    "CONSTRAINT" ...
    "PRIMARY" ...
    "UNIQUE" ...
    "WATERMARK" ...
    <BRACKET_QUOTED_IDENTIFIER> ...
    <QUOTED_IDENTIFIER> ...
    <BACK_QUOTED_IDENTIFIER> ...
    <BIG_QUERY_BACK_QUOTED_IDENTIFIER> ...
    <HYPHENATED_IDENTIFIER> ...
    <IDENTIFIER> ...
    <UNICODE_QUOTED_IDENTIFIER> ...

Flink SQL> CREATE TABLE kafka_bank_txn (
>   transactionId STRING,
>   `timestamp` TIMESTAMP(3),
>   accountId STRING,
>   sourceAccount STRING,
>   destinationAccount STRING,
>   amount DOUBLE,
>   currency STRING,
>   transactionType STRING,
>   channel STRING,
>   location ROW<ip STRING, city STRING, country STRING>,
>   status STRING,
>   fraudFlag BOOLEAN,
>   fraudReason STRING,
>   fraudScore DOUBLE,
>   verdict STRING
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'bank-transactions',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'properties.group.id' = 'fraud-filter-group',
>   'scan.startup.mode' = 'earliest-offset',
>   'format' = 'json',
>   'json.ignore-parse-errors' = 'true'
> );
>
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE fraud_mv (
>   transactionId STRING,
>   accountId STRING,
>   amount DOUBLE,
>   fraudScore DOUBLE,
>   fraudReason STRING,
>   location_city STRING,
>   location_country STRING,
>   PRIMARY KEY (transactionId) NOT ENFORCED
> ) WITH (
>   'connector' = 'upsert-kafka',
>   'topic' = 'fraud-mv-topic',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'key.format' = 'json',
>   'value.format' = 'json'
> );
>
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO fraud_mv
> SELECT
>   transactionId,
>   accountId,
>   amount,
>   fraudScore,
>   fraudReason,
>   location.city AS location_city,
>   location.country AS location_country
> FROM kafka_bank_txn
> WHERE fraudFlag = TRUE
>   AND status = 'BLOCKED'
>   AND verdict = 'FRAUD';
>
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 03e70759f7e2e43b041153642f215801


Flink SQL>

```


Producer 

```
C:\tmp\flink-sql\kafka-scripts>python bankTransactionGen.py
C:\tmp\flink-sql\kafka-scripts\bankTransactionGen.py:20: DeprecationWarning: datetime.datetime.utcnow() is deprecated and scheduled for removal in a future version. Use timezone-aware objects to represent datetimes in UTC: datetime.datetime.now(datetime.UTC).
  "timestamp": datetime.utcnow().isoformat() + "Z",
C:\tmp\flink-sql\kafka-scripts\bankTransactionGen.py:44: DeprecationWarning: datetime.datetime.utcnow() is deprecated and scheduled for removal in a future version. Use timezone-aware objects to represent datetimes in UTC: datetime.datetime.now(datetime.UTC).
  "timestamp": datetime.utcnow().isoformat() + "Z",
Sending: {
  "transactionId": "34f02bb3-1d06-4217-9402-661390e67a9c",
  "timestamp": "2025-05-06T17:10:43.722474Z",
  "accountId": "WGUL75273221148978",
  "sourceAccount": "GB78ABXY75114260291057",
  "destinationAccount": "GB88KSFZ10976616396132",
  "amount": 464.76,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "WEB",
  "location": {
    "ip": "48.78.213.169",
    "city": "Lake Alexandraville",
    "country": "PK"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "6b2c3d6b-46ba-47a6-898d-5a1f774f37a3",
  "timestamp": "2025-05-06T17:10:43.728322Z",
  "accountId": "IHEZ01111850787885",
  "sourceAccount": "GB95FNQD32863177856349",
  "destinationAccount": "GB58ABVP73026959095332",
  "amount": 6816.69,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "111.163.157.249",
    "city": "Bradfordborough",
    "country": "TN"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "HighRiskCountry + UnusualAmount",
  "fraudScore": 0.94,
  "verdict": "REVIEW"
}
Sending: {
  "transactionId": "d2ccaa1c-aa82-499c-b2c4-e3a0eaefec78",
  "timestamp": "2025-05-06T17:10:43.725203Z",
  "accountId": "VHUX43736436339601",
  "sourceAccount": "GB91HWDV62518999424031",
  "destinationAccount": "GB39VGBS02193540076317",
  "amount": 3000.91,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "24.171.200.148",
    "city": "East Tommy",
    "country": "KZ"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.87,
  "verdict": "FRAUD"
}
Sending: {
  "transactionId": "77220fcb-bbee-4ad5-a0b2-0e1112820b4a",
  "timestamp": "2025-05-06T17:10:43.720217Z",
  "accountId": "SFWU78824866280428",
  "sourceAccount": "GB69OTLA43412257434353",
  "destinationAccount": "GB02ROIY99710692701119",
  "amount": 469.03,
  "currency": "USD",
  "transactionType": "BILL_PAYMENT",
  "channel": "ATM",
  "location": {
    "ip": "134.157.44.214",
    "city": "Mathisville",
    "country": "CU"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "eb4a25d1-7bf1-4747-85ca-69c3557789b2",
  "timestamp": "2025-05-06T17:10:43.712797Z",
  "accountId": "QOCY91996216930067",
  "sourceAccount": "GB86HLNI38399289541153",
  "destinationAccount": "GB54QFNL19339644958257",
  "amount": 421.0,
  "currency": "USD",
  "transactionType": "POS_PURCHASE",
  "channel": "ATM",
  "location": {
    "ip": "218.144.54.66",
    "city": "Melissamouth",
    "country": "LV"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "2c248163-24f9-4fbc-9406-b80a2220ff6a",
  "timestamp": "2025-05-06T17:10:43.720217Z",
  "accountId": "OIRD50062282405496",
  "sourceAccount": "GB36RIFV92377088545357",
  "destinationAccount": "GB65NLGT19461772359808",
  "amount": 178.17,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "ATM",
  "location": {
    "ip": "167.221.1.103",
    "city": "West Isaiah",
    "country": "SL"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "c65b5004-114b-4af6-8cc0-21cf937a936d",
  "timestamp": "2025-05-06T17:10:43.725857Z",
  "accountId": "PNBM55984736244208",
  "sourceAccount": "GB43DDPN61392578612785",
  "destinationAccount": "GB74XGVA10011987196238",
  "amount": 8053.93,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "84.238.208.217",
    "city": "Devinport",
    "country": "BR"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "HighRiskCountry + UnusualAmount",
  "fraudScore": 0.97,
  "verdict": "FRAUD"
}
Sending: {
  "transactionId": "ee0287f3-f338-424f-8b09-55c55adf96f1",
  "timestamp": "2025-05-06T17:10:43.726494Z",
  "accountId": "NEUW34180287885935",
  "sourceAccount": "GB37XWDO65081484158618",
  "destinationAccount": "GB33VNCO95556363800648",
  "amount": 4043.29,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "146.235.79.203",
    "city": "Klineside",
    "country": "BG"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.91,
  "verdict": "REVIEW"
}
Sending: {
  "transactionId": "f8ed6a55-8457-4c03-b09b-d2f7664b24ef",
  "timestamp": "2025-05-06T17:10:43.720217Z",
  "accountId": "AHMO56832291588360",
  "sourceAccount": "GB45YGKG43559444465843",
  "destinationAccount": "GB60XYQF91140978453975",
  "amount": 274.23,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "CARD_TERMINAL",
  "location": {
    "ip": "167.10.119.68",
    "city": "Michelleton",
    "country": "SV"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "e91e24d3-5afe-49c9-ae81-77cf06a502d9",
  "timestamp": "2025-05-06T17:10:43.727764Z",
  "accountId": "MDWY02326173226272",
  "sourceAccount": "GB95QLKF90064805757279",
  "destinationAccount": "GB69RGRF59551508441266",
  "amount": 3288.19,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "217.43.195.94",
    "city": "Port Ann",
    "country": "MN"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.99,
  "verdict": "REVIEW"
}
Sending: {
  "transactionId": "f324a82c-49bb-47dd-a313-dfcf893f47c9",
  "timestamp": "2025-05-06T17:10:43.723990Z",
  "accountId": "LNSN52035398585372",
  "sourceAccount": "GB42IWFU45015425001446",
  "destinationAccount": "GB13YWLV46849502311524",
  "amount": 9585.4,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "101.56.226.167",
    "city": "East Alexisland",
    "country": "IQ"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "HighRiskCountry + UnusualAmount",
  "fraudScore": 0.88,
  "verdict": "FRAUD"
}
Sending: {
  "transactionId": "6965ab04-74ae-4b9c-940f-168cab4f3c20",
  "timestamp": "2025-05-06T17:10:43.727139Z",
  "accountId": "EYCL63727427173886",
  "sourceAccount": "GB33QSSF86753271559609",
  "destinationAccount": "GB69CZEB65824411120542",
  "amount": 6421.01,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "198.169.64.78",
    "city": "North Sarahfurt",
    "country": "PH"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.87,
  "verdict": "FRAUD"
}
Sending: {
  "transactionId": "10c01b58-c884-4ba0-bbbf-0fca2a69c8e9",
  "timestamp": "2025-05-06T17:10:43.724561Z",
  "accountId": "IZDU00760674885575",
  "sourceAccount": "GB50EWFJ25758699132119",
  "destinationAccount": "GB85JMIJ28253527671084",
  "amount": 9967.05,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "12.29.87.71",
    "city": "South Kennethstad",
    "country": "CU"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.95,
  "verdict": "REVIEW"
}
Sending: {
  "transactionId": "ff226a3d-cb36-4d8e-8341-da5ab67043d5",
  "timestamp": "2025-05-06T17:10:43.719088Z",
  "accountId": "EGXV33701187432646",
  "sourceAccount": "GB50FAWS63992362103586",
  "destinationAccount": "GB03FWRW88943930129459",
  "amount": 178.97,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "ATM",
  "location": {
    "ip": "154.184.225.244",
    "city": "West Carl",
    "country": "MD"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "4fa233d7-1da7-4190-9a77-61eef4fe0d4d",
  "timestamp": "2025-05-06T17:10:43.727139Z",
  "accountId": "JGSR35900930272251",
  "sourceAccount": "GB61XXHR45906605742353",
  "destinationAccount": "GB97MBVV52670566143341",
  "amount": 5115.58,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "219.93.170.25",
    "city": "Ericbury",
    "country": "MA"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.92,
  "verdict": "REVIEW"
}
Sending: {
  "transactionId": "a4188fe5-18f6-43eb-8dca-eaf460976b13",
  "timestamp": "2025-05-06T17:10:43.713348Z",
  "accountId": "UQZN17217800526637",
  "sourceAccount": "GB16DFOH53294972267358",
  "destinationAccount": "GB40ZMHS72925626370408",
  "amount": 425.22,
  "currency": "USD",
  "transactionType": "BILL_PAYMENT",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "31.138.6.230",
    "city": "Nicholsville",
    "country": "ER"
  },
  "status": "COMPLETED"
}
Sending: {
  "transactionId": "256246ee-5889-4da8-ae19-28ecadfd6399",
  "timestamp": "2025-05-06T17:10:43.725857Z",
  "accountId": "YNBX89972210690611",
  "sourceAccount": "GB88KIDT97320850394453",
  "destinationAccount": "GB09DHCU43177089149792",
  "amount": 5880.94,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "206.124.207.232",
    "city": "West Katieport",
    "country": "AE"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "NewDeviceLogin + HighAmount",
  "fraudScore": 0.93,
  "verdict": "REVIEW"
}
Sending: {
  "transactionId": "78f6b91a-5e95-471a-8934-4eaa5c11dde9",
  "timestamp": "2025-05-06T17:10:43.725857Z",
  "accountId": "SKGC35770153902044",
  "sourceAccount": "GB27GLRB61785056843082",
  "destinationAccount": "GB83GDGM00731472419827",
  "amount": 6049.49,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "201.173.158.194",
    "city": "North Jessica",
    "country": "TR"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "HighRiskCountry + UnusualAmount",
  "fraudScore": 0.95,
  "verdict": "FRAUD"
}
Sending: {
  "transactionId": "0c3a5653-cb35-4ea9-b68f-5e0aba3833f8",
  "timestamp": "2025-05-06T17:10:43.725203Z",
  "accountId": "RKWU84148069293251",
  "sourceAccount": "GB74JNAJ93646965567494",
  "destinationAccount": "GB22NSMR51795008186848",
  "amount": 6241.27,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "MOBILE_APP",
  "location": {
    "ip": "146.83.127.135",
    "city": "Chadfort",
    "country": "GA"
  },
  "status": "BLOCKED",
  "fraudFlag": true,
  "fraudReason": "VelocityPatternDetected",
  "fraudScore": 0.93,
  "verdict": "FRAUD"
}
Sending: {
  "transactionId": "5121b760-07ba-4d0a-8856-04f247d4d0f2",
  "timestamp": "2025-05-06T17:10:43.700483Z",
  "accountId": "ANAW21004292038059",
  "sourceAccount": "GB82YLAD52474675846513",
  "destinationAccount": "GB98ELII92350832805131",
  "amount": 171.58,
  "currency": "USD",
  "transactionType": "TRANSFER",
  "channel": "ATM",
  "location": {
    "ip": "211.169.221.18",
    "city": "Jeanettestad",
    "country": "JM"
  },
  "status": "COMPLETED"
}
✅ All transactions sent.

C:\tmp\flink-sql\kafka-scripts>

```


Consumer 

```
C:\tmp\flink-sql\kafka-scripts>python consumer.py
 Listening to 'my-topic'... Press Ctrl+C to exit.

🔹 Received message: {'transactionId': '000528d5-67ad-4fa8-80e1-eb7445088ded', 'accountId': 'HXIT46154868010634', 'amount': 3304.06, 'fraudScore': 0.92, 'fraudReason': 'NewDeviceLogin + HighAmount', 'location_city': 'Josechester', 'location_country': 'SA'}
🔹 Received message: {'transactionId': '17d52bcb-0a23-437f-b137-7f23c1deaf6e', 'accountId': 'DKSF89593615618351', 'amount': 3068.21, 'fraudScore': 0.92, 'fraudReason': 'VelocityPatternDetected', 'location_city': 'East Ericchester', 'location_country': 'FR'}
🔹 Received message: {'transactionId': 'ff79a5fa-0f1f-410d-831d-7f6cfd29d35b', 'accountId': 'MUNB30596234451882', 'amount': 9056.78, 'fraudScore': 0.9, 'fraudReason': 'HighRiskCountry + UnusualAmount', 'location_city': 'New Monicastad', 'location_country': 'MH'}
🔹 Received message: {'transactionId': '93ee3a1b-355d-42c3-865e-b7faf9daae8c', 'accountId': 'PEBB04130478912853', 'amount': 7290.4, 'fraudScore': 0.97, 'fraudReason': 'VelocityPatternDetected', 'location_city': 'West Heathershire', 'location_country': 'GT'}
🔹 Received message: {'transactionId': 'ef75f90f-17ca-4285-ba5e-4ed5638a50f2', 'accountId': 'HOEZ65921660441552', 'amount': 6118.19, 'fraudScore': 0.86, 'fraudReason': 'HighRiskCountry + UnusualAmount', 'location_city': 'Montoyaburgh', 'location_country': 'ER'}
🔹 Received message: {'transactionId': '20c7eb75-0087-4442-a236-bc378f034480', 'accountId': 'DKBW37651627989375', 'amount': 9933.68, 'fraudScore': 0.96, 'fraudReason': 'HighRiskCountry + UnusualAmount', 'location_city': 'West Angela', 'location_country': 'US'}
🔹 Received message: {'transactionId': '87459e14-36cd-4fe7-b04a-f0065f434aed', 'accountId': 'HAEJ69358787435192', 'amount': 5579.31, 'fraudScore': 0.87, 'fraudReason': 'VelocityPatternDetected', 'location_city': 'Mooreborough', 'location_country': 'KN'} 
🔹 Received message: {'transactionId': 'd2ccaa1c-aa82-499c-b2c4-e3a0eaefec78', 'accountId': 'VHUX43736436339601', 'amount': 3000.91, 'fraudScore': 0.87, 'fraudReason': 'VelocityPatternDetected', 'location_city': 'East Tommy', 'location_country': 'KZ'}   
🔹 Received message: {'transactionId': 'c65b5004-114b-4af6-8cc0-21cf937a936d', 'accountId': 'PNBM55984736244208', 'amount': 8053.93, 'fraudScore': 0.97, 'fraudReason': 'HighRiskCountry + UnusualAmount', 'location_city': 'Devinport', 'location_country': 'BR'}
🔹 Received message: {'transactionId': 'f324a82c-49bb-47dd-a313-dfcf893f47c9', 'accountId': 'LNSN52035398585372', 'amount': 9585.4, 'fraudScore': 0.88, 'fraudReason': 'HighRiskCountry + UnusualAmount', 'location_city': 'East Alexisland', 'location_country': 'IQ'}
🔹 Received message: {'transactionId': '6965ab04-74ae-4b9c-940f-168cab4f3c20', 'accountId': 'EYCL63727427173886', 'amount': 6421.01, 'fraudScore': 0.87, 'fraudReason': 'VelocityPatternDetected', 'location_city': 'North Sarahfurt', 'location_country': 'PH'}
🔹 Received message: {'transactionId': '78f6b91a-5e95-471a-8934-4eaa5c11dde9', 'accountId': 'SKGC35770153902044', 'amount': 6049.49, 'fraudScore': 0.95, 'fraudReason': 'HighRiskCountry + UnusualAmount', 'location_city': 'North Jessica', 'location_country': 'TR'}
🔹 Received message: {'transactionId': '0c3a5653-cb35-4ea9-b68f-5e0aba3833f8', 'accountId': 'RKWU84148069293251', 'amount': 6241.27, 'fraudScore': 0.93, 'fraudReason': 'VelocityPatternDetected', 'location_city': 'Chadfort', 'location_country': 'GA'}     

```
