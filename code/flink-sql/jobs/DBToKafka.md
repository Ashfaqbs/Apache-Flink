Docker 


C:\tmp\flink-sql\docker>docker-compose up -d
time="2025-04-30T15:37:22+05:30" level=warning msg="C:\\tmp\\flink-sql\\docker\\docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion"
[+] Running 7/7
 ✔ Network docker_default          Created                                                                                0.1s 
 ✔ Container docker-zookeeper-1    Started                                                                                1.0s 
 ✔ Container docker-postgres-1     Started                                                                                1.0s 
 ✔ Container docker-kafka-1        Started                                                                                1.0s 
 ✔ Container docker-jobmanager-1   Started                                                                                1.2s 
 ✔ Container docker-taskmanager-2  Started                                                                                1.9s 
 ✔ Container docker-taskmanager-1  Started                                                                                1.5s 

C:\tmp\flink-sql\docker>



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

mainschema=# INSERT INTO users (name, email) VALUES
('Karen Hill', 'karen@example.com'),
('Robert Smith', 'robert@example.org'),
('Emily Jones', 'emily.jones@example.net'),
('James Brown', 'jamesb@example.com'),
('Linda Garcia', 'linda.garcia@example.org'),
('Michael Miller', 'mike.miller@example.net'),
('Sarah Wilson', 'sarahw@example.com'),
('David Martinez', 'david.m@example.org'),
('Lisa Anderson', 'lisa.a@example.net'),
('Daniel Taylor', 'danielt@example.com'),
('Paul Thomas', 'paul.t@example.org'),
('Nancy Jackson', 'nancy.j@example.net'),
('Mark White', 'markw@example.com'),
('Laura Harris', 'laura.h@example.org'),
('Steven Martin', 'stevenm@example.net'),
('Karen Thompson', 'karent@example.com'),
('Edward Garcia', 'edwardg@example.org'),
('Betty Martinez', 'bettym@example.net'),
('Brian Robinson', 'brianr@example.com'),
('Sandra Clark', 'sandra.c@example.org'),
('Anthony Rodriguez', 'anthony.r@example.net'),
('Jessica Lewis', 'jessical@example.com'),
('Kevin Lee', 'kevin.l@example.org'),
('Dorothy Walker', 'dorothyw@example.net'),
('George Hall', 'georgeh@example.com'),
('Amy Allen', 'amya@example.org'),
('Frank Young', 'franky@example.net'),
('Deborah Hernandez', 'deborah.h@example.com'),
('Jerry King', 'jerryk@example.org'),
('Cynthia Wright', 'cynthiaw@example.net'),
('Matthew Lopez', 'matthew.l@example.com'),
('Angela Hill', 'angelah@example.org'),
('Raymond Scott', 'raymond.s@example.net'),
('Sharon Green', 'sharong@example.com'),
('Gregory Adams', 'grega@example.org'),
('Michelle Baker', 'michelleb@example.net'),
('Joshua Gonzalez', 'joshua.g@example.com'),
('Laura Nelson', 'lauran@example.org'),
('Patrick Carter', 'patrick.c@example.net'),
('Rebecca Mitchell', 'rebecca.m@example.com'),
('Ruth Stewart', 'ruth.s@example.org');

INSERT 0 50

mainschema=# \dt;
         List of relations
 Schema | Name  | Type  |  Owner
--------+-------+-------+----------
 public | users | table | postgres
(1 row)

mainschema=# select * from users;
mainschema=# select count(*) from users;
 count
-------
    50
(1 row)



FLINK SQL 


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

Flink SQL> CREATE TABLE users_source (
>   name STRING,
>   email STRING
> ) WITH (
>   'connector' = 'jdbc',
>   'url' = 'jdbc:postgresql://postgres:5432/mainschema',
>   'table-name' = 'users',
>   'username' = 'postgres',
>   'password' = 'admin',
>   'scan.fetch-size' = '10'
> );
>
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE kafka_sink (
>   name STRING,
>   email STRING
> ) WITH (
>   'connector' = 'kafka',
>   'topic' = 'n-topic',
>   'properties.bootstrap.servers' = 'kafka:9093',
>   'format' = 'json'
> );
>
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO kafka_sink
> SELECT name, email FROM users_source;
>
[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.table.api.ValidationException: Could not find any factory for identifier 'jdbc' that implements 'org.apache.flink.table.factories.DynamicTableFactory' in the classpath.

Available factory identifiers are:

blackhole
datagen
filesystem
print
python-input-format


Paste the jars from the custom folder to lib of jobmanager container

Flink SQL> ADD JAR '/opt/flink/custom-lib/flink-sql-connector-kafka-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL> ADD JAR '/opt/flink/custom-lib/flink-connector-jdbc-3.3.0-1.19.jar';
[INFO] Execute statement succeed.

Flink SQL>  ADD JAR '/opt/flink/custom-lib/postgresql-42.6.0.jar';
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO kafka_sink
> SELECT name, email FROM users_source;
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: ed4f382b1febcd8d7940a66e432fd83a


Flink SQL>


Flink UI

![alt text](/jobs/images/db2k.png)














Kafka 


C:\tmp\flink-sql\kafka-scripts>python consumer.py
✅ Listening to 'my-topic'... Press Ctrl+C to exit.

🔹 Received message: {'name': 'Karen Hill', 'email': 'karen@example.com'}
🔹 Received message: {'name': 'Robert Smith', 'email': 'robert@example.org'}
🔹 Received message: {'name': 'Emily Jones', 'email': 'emily.jones@example.net'}
🔹 Received message: {'name': 'James Brown', 'email': 'jamesb@example.com'}
🔹 Received message: {'name': 'Linda Garcia', 'email': 'linda.garcia@example.org'}
🔹 Received message: {'name': 'Michael Miller', 'email': 'mike.miller@example.net'}
🔹 Received message: {'name': 'Sarah Wilson', 'email': 'sarahw@example.com'}
🔹 Received message: {'name': 'David Martinez', 'email': 'david.m@example.org'}
🔹 Received message: {'name': 'Lisa Anderson', 'email': 'lisa.a@example.net'}
🔹 Received message: {'name': 'Daniel Taylor', 'email': 'danielt@example.com'}
🔹 Received message: {'name': 'Paul Thomas', 'email': 'paul.t@example.org'}
🔹 Received message: {'name': 'Nancy Jackson', 'email': 'nancy.j@example.net'}
🔹 Received message: {'name': 'Mark White', 'email': 'markw@example.com'}
🔹 Received message: {'name': 'Laura Harris', 'email': 'laura.h@example.org'}
🔹 Received message: {'name': 'Steven Martin', 'email': 'stevenm@example.net'}
🔹 Received message: {'name': 'Karen Thompson', 'email': 'karent@example.com'}
🔹 Received message: {'name': 'Edward Garcia', 'email': 'edwardg@example.org'}
🔹 Received message: {'name': 'Betty Martinez', 'email': 'bettym@example.net'}
🔹 Received message: {'name': 'Brian Robinson', 'email': 'brianr@example.com'}
🔹 Received message: {'name': 'Sandra Clark', 'email': 'sandra.c@example.org'}
🔹 Received message: {'name': 'Anthony Rodriguez', 'email': 'anthony.r@example.net'}
🔹 Received message: {'name': 'Jessica Lewis', 'email': 'jessical@example.com'}
🔹 Received message: {'name': 'Kevin Lee', 'email': 'kevin.l@example.org'}
🔹 Received message: {'name': 'Dorothy Walker', 'email': 'dorothyw@example.net'}
🔹 Received message: {'name': 'George Hall', 'email': 'georgeh@example.com'}
🔹 Received message: {'name': 'Amy Allen', 'email': 'amya@example.org'}
🔹 Received message: {'name': 'Frank Young', 'email': 'franky@example.net'}
🔹 Received message: {'name': 'Deborah Hernandez', 'email': 'deborah.h@example.com'}
🔹 Received message: {'name': 'Jerry King', 'email': 'jerryk@example.org'}
🔹 Received message: {'name': 'Cynthia Wright', 'email': 'cynthiaw@example.net'}
🔹 Received message: {'name': 'Matthew Lopez', 'email': 'matthew.l@example.com'}
🔹 Received message: {'name': 'Angela Hill', 'email': 'angelah@example.org'}
🔹 Received message: {'name': 'Raymond Scott', 'email': 'raymond.s@example.net'}
🔹 Received message: {'name': 'Sharon Green', 'email': 'sharong@example.com'}
🔹 Received message: {'name': 'Gregory Adams', 'email': 'grega@example.org'}
🔹 Received message: {'name': 'Michelle Baker', 'email': 'michelleb@example.net'}
🔹 Received message: {'name': 'Joshua Gonzalez', 'email': 'joshua.g@example.com'}
🔹 Received message: {'name': 'Laura Nelson', 'email': 'lauran@example.org'}
🔹 Received message: {'name': 'Patrick Carter', 'email': 'patrick.c@example.net'}
🔹 Received message: {'name': 'Rebecca Mitchell', 'email': 'rebecca.m@example.com'}
🔹 Received message: {'name': 'Dennis Perez', 'email': 'dennisp@example.org'}
🔹 Received message: {'name': 'Kimberly Roberts', 'email': 'kimr@example.net'}
🔹 Received message: {'name': 'Jason Turner', 'email': 'jasont@example.com'}
🔹 Received message: {'name': 'Shirley Phillips', 'email': 'shirleyp@example.org'}
🔹 Received message: {'name': 'Brandon Campbell', 'email': 'brandon.c@example.net'}
🔹 Received message: {'name': 'Anna Parker', 'email': 'annap@example.com'}
🔹 Received message: {'name': 'Samuel Evans', 'email': 'samuel.e@example.org'}
🔹 Received message: {'name': 'Pamela Edwards', 'email': 'pamelae@example.net'}
🔹 Received message: {'name': 'Stephen Collins', 'email': 'stephenc@example.com'}
🔹 Received message: {'name': 'Ruth Stewart', 'email': 'ruth.s@example.org'}
