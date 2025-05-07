PS C:\Users\ashfa> docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED          STATUS          PORTS                                                NAMES
687f1346b7d1   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   11 seconds ago   Up 10 seconds   6123/tcp, 8081/tcp                                   docker-taskmanager-2
22876acb6aaa   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   11 seconds ago   Up 9 seconds    6123/tcp, 8081/tcp                                   docker-taskmanager-1
ead02749ca74   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   11 seconds ago   Up 10 seconds   6123/tcp, 0.0.0.0:8081->8081/tcp                     docker-jobmanager-1
c3c198a8abd8   wurstmeister/zookeeper:latest   "/bin/sh -c '/usr/sb…"   11 seconds ago   Up 10 seconds   22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   docker-zookeeper-1
cac11f27e2d9   postgres:latest                 "docker-entrypoint.s…"   11 seconds ago   Up 10 seconds   0.0.0.0:5432->5432/tcp                               docker-postgres-1
e970f9618fa2   wurstmeister/kafka:latest       "start-kafka.sh"         11 seconds ago   Up 10 seconds   0.0.0.0:9092->9092/tcp, 9093/tcp                     docker-kafka-1
384a39d7fa4c   mongo:latest                    "docker-entrypoint.s…"   11 seconds ago   Up 10 seconds   0.0.0.0:27017->27017/tcp                             docker-mongo-1
PS C:\Users\ashfa> docker exec -it docker-mongo-1 mongosh
Current Mongosh Log ID: 6811fb0b486c2ad294964032
Connecting to:          mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+2.3.1
Using MongoDB:          8.0.0
Using Mongosh:          2.3.1

For mongosh info see: https://www.mongodb.com/docs/mongodb-shell/


To help improve our products, anonymous usage data is collected and sent to MongoDB periodically (https://www.mongodb.com/legal/privacy-policy).
You can opt-out by running the disableTelemetry() command.

------
   The server generated these startup warnings when booting
   2025-04-30T10:26:43.438+00:00: Using the XFS filesystem is strongly recommended with the WiredTiger storage engine. See http://dochub.mongodb.org/core/prodnotes-filesystem
   2025-04-30T10:26:44.372+00:00: Access control is not enabled for the database. Read and write access to data and configuration is unrestricted
   2025-04-30T10:26:44.372+00:00: For customers running the updated tcmalloc-google memory allocator, we suggest setting the contents of sysfsFile to 'defer+madvise'
   2025-04-30T10:26:44.372+00:00: We suggest setting the contents of sysfsFile to 0.
   2025-04-30T10:26:44.372+00:00: Your system has glibc support for rseq built in, which is not yet supported by tcmalloc-google and has critical performance implications. Please set the environment variable GLIBC_TUNABLES=glibc.pthread.rseq=0
   2025-04-30T10:26:44.372+00:00: vm.max_map_count is too low
   2025-04-30T10:26:44.372+00:00: We suggest setting swappiness to 0 or 1, as swapping can cause performance problems.
------

test> use mydb
switched to db mydb
mydb> db.user_data.find().pretty()
[
  {
    _id: ObjectId('6811fae2046b56206b964033'),
    fname: 'John',
    lname: 'Doe',
    email: 'john@example.com'
  },
  {
    _id: ObjectId('6811fae2046b56206b964034'),
    fname: 'Alice',
    lname: 'Smith',
    email: 'alice@example.com'
  },
  {
    _id: ObjectId('6811fae2046b56206b964035'),
    fname: 'Bob',
    lname: 'Johnson',
    email: 'bob@example.com'
  },
  {
    _id: ObjectId('6811fae2046b56206b964036'),
    fname: 'Charlie',
    lname: 'Ray',
    email: ''
  },
  {
    _id: ObjectId('6811fae2046b56206b964037'),
    fname: 'Eva',
    lname: 'Green',
    email: null
  }
]
mydb>








Postgres DB

PS C:\Users\ashfa> docker exec -it docker-postgres-1 psql -U postgres -d mainschema
psql (17.0 (Debian 17.0-1.pgdg120+1))
Type "help" for help.

mainschema=# \dt;
         List of relations
 Schema | Name  | Type  |  Owner
--------+-------+-------+----------
 public | users | table | postgres
(1 row)

mainschema=# select * from users;
 name | email
------+-------
(0 rows)

mainschema=#





Add mongo and postgres jar from customlib to flink job manager lib


ADD JAR '/opt/flink/custom-lib/postgresql-42.6.0.jar';
ADD JAR '/opt/flink/custom-lib/flink-sql-connector-mongodb-1.2.0-1.19.jar';


Flink 

Flink SQL> CREATE TABLE mongo_users (
>     fname STRING,
>     lname STRING,
>     email STRING
> ) WITH (
>     'connector' = 'mongodb',
>     'uri' = 'mongodb://mongo:27017',
>     'database' = 'mydb',
>     'collection' = 'user_data'
> );
>
[INFO] Execute statement succeed.

Flink SQL> CREATE TABLE pg_users (
>     name STRING,
>     email STRING
> ) WITH (
>     'connector' = 'jdbc',
>     'url' = 'jdbc:postgresql://postgres:5432/mainschema',
>     'table-name' = 'users',
>     'username' = 'postgres',
>     'password' = 'admin',
>     'driver' = 'org.postgresql.Driver'
> );
[INFO] Execute statement succeed.


Flink SQL> INSERT INTO pg_users
> SELECT
>     UPPER(CONCAT(fname, ' ', lname)) AS name,
>     LOWER(email) AS email
> FROM mongo_users
> WHERE email IS NOT NULL AND TRIM(email) <> '';
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 1898ddc3690b6161ec088bc28e25e29a



FLINK UI


![alt text](/jobs/images/mongo2pg.png)

postgres DB

mainschema=# select * from users;
 name | email
------+-------
(0 rows)

post running the job
mainschema=# select * from users;
    name     |       email
-------------+-------------------
 JOHN DOE    | john@example.com
 ALICE SMITH | alice@example.com
 BOB JOHNSON | bob@example.com
(3 rows)

mainschema=#


