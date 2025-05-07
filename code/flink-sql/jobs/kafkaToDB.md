
Setup docker for kafka and flink 

PS C:\Users\ashfa> docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED         STATUS         PORTS                                                NAMES
049b42fa93e0   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   4 minutes ago   Up 4 minutes   6123/tcp, 8081/tcp                                   docker-taskmanager-2
6dbfe58d8b45   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   4 minutes ago   Up 4 minutes   6123/tcp, 8081/tcp                                   docker-taskmanager-1
c24f417620a0   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   4 minutes ago   Up 4 minutes   6123/tcp, 0.0.0.0:8081->8081/tcp                     docker-jobmanager-1
f327c508b863   wurstmeister/zookeeper:latest   "/bin/sh -c '/usr/sb…"   4 minutes ago   Up 4 minutes   22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   docker-zookeeper-1
7e76c3b598a3   wurstmeister/kafka:latest       "start-kafka.sh"         4 minutes ago   Up 4 minutes   0.0.0.0:9092->9092/tcp, 9093/tcp                     docker-kafka-1
45f9ec52be8d   postgres:latest                 "docker-entrypoint.s…"   4 minutes ago   Up 4 minutes   0.0.0.0:5432->5432/tcp                               docker-postgres-1
PS C:\Users\ashfa> docker exec -it docker-kafka-1 kafka-topics.sh --list --bootstrap-server localhost:9092
my-topic



paste the jar files to the custom folder and paste to lib of jobmanager container pasting directly will remove the existing jars.



PS C:\Users\ashfa> docker exec -it docker-jobmanager-1 ./bin/sql-client.sh
\WARNING: Unknown module: jdk.compiler specified to --add-exports
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

Flink SQL> CREATE TABLE kafka_users (
>   name STRING,
>   email STRING,
>   role STRING
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'my-topic',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'properties.group.id' = 'flink-group',
>   'scan.startup.mode' = 'earliest-offset',
>   'format' = 'json',
>   'json.ignore-parse-errors' = 'true'
> );
>
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE users_sink (
>   name STRING,
>   email STRING
> ) WITH (
>   'connector' = 'jdbc',
>   'url' = 'jdbc:postgresql://postgres:5432/mainschema',
>   'table-name' = 'users',
>   'username' = 'postgres',
>   'password' = 'admin',
>   'driver' = 'org.postgresql.Driver'
> );
>
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO users_sink
> SELECT name, email
> FROM kafka_users
> WHERE role = 'developer';
>
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

Flink SQL> ADD JAR '/opt/flink/custom-lib/flink-connector-jdbc-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL> ADD JAR '/opt/flink/custom-lib/postgresql-42.6.0.jar';
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO users_sink
> SELECT name, email
> FROM kafka_users
> WHERE role = 'developer';
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 1d025e2baeb5ff30a1256208c43edf2c


Flink SQL>





Producer script

C:\tmp\flink-sql\kafka-scripts>python producer.py
Sending: {'name': 'David Walker', 'email': 'joshuaross@example.net', 'role': 'tester'}
Sending: {'name': 'David Pennington', 'email': 'pberg@example.net', 'role': 'tester'}
Sending: {'name': 'Karina Hines', 'email': 'andersonsarah@example.com', 'role': 'manager'}
Sending: {'name': 'Justin Keller', 'email': 'sheilafarmer@example.org', 'role': 'analyst'}
Sending: {'name': 'Bethany Sanders MD', 'email': 'jenniferhuber@example.org', 'role': 'tester'}
Sending: {'name': 'Catherine Noble', 'email': 'maureen98@example.net', 'role': 'analyst'}
Sending: {'name': 'Karen Hill', 'email': 'moraemily@example.org', 'role': 'developer'}
Sending: {'name': 'Ryan Lee', 'email': 'alexandrawilson@example.net', 'role': 'manager'}
Sending: {'name': 'Terry Parrish', 'email': 'edward95@example.com', 'role': 'tester'}
Sending: {'name': 'Devon Rhodes', 'email': 'ypierce@example.net', 'role': 'analyst'}
✅ All messages sent.

C:\tmp\flink-sql\kafka-scripts>

DB 

PS C:\Users\ashfa> docker exec -it docker-postgres-1 psql -U postgres -d mainschema
psql (17.0 (Debian 17.0-1.pgdg120+1))
Type "help" for help.

mainschema=# SELECT * FROM users LIMIT 10;
 name | email
------+-------
(0 rows)

mainschema=# SELECT * FROM users LIMIT 10;
    name    |         email
------------+-----------------------
 Karen Hill | moraemily@example.org
(1 row)

mainschema=# SELECT * FROM users LIMIT 10;
    name    |         email
------------+-----------------------
 Karen Hill | moraemily@example.org
(1 row)

mainschema=#



Flink UI

![alt text](/jobs/images/kafka2db.png)