- Flink SQL Gateway:

```
Exposes a REST and JDBC API to submit SQL statements to Flink clusters.

Is ideal for integrating with apps (e.g., web UI, backend service).

Decouples job submission from CLI (unlike sql-client.sh).
```


- Jar link : https://mvnrepository.com/artifact/org.apache.flink/flink-sql-gateway-api/1.19.2

- Resources Link:

```
https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/sql-gateway/rest/
https://github.com/ververica/flink-sql-gateway/blob/master/README.md
https://www.decodable.co/blog/exploring-the-flink-sql-gateway-rest-api
```

- paste in lib folder of jobmanager

```
PS C:\Users\ashfa\Downloads> docker cp .\flink-sql-gateway-api-1.19.2.jar fc1613c83355:/opt/flink/lib/
Successfully copied 40.4kB to fc1613c83355:/opt/flink/lib/



Verify

root@fc1613c83355:/opt/flink# ls lib | grep sql-gateway
flink-sql-gateway-api-1.19.2.jar
root@fc1613c83355:/opt/flink# ls
bin  conf  custom-lib  examples  lib  LICENSE  licenses  log  NOTICE  opt  plugins  README.txt
root@fc1613c83355:/opt/flink# ls lib
flink-cep-1.19.2.jar                 flink-scala_2.12-1.19.2.jar                 flink-table-runtime-1.19.2.jar
flink-connector-files-1.19.2.jar     flink-sql-connector-kafka-3.3.0-1.19.jar    log4j-1.2-api-2.17.1.jar
flink-connector-jdbc-3.3.0-1.19.jar  flink-sql-connector-mongodb-1.2.0-1.19.jar  log4j-api-2.17.1.jar
flink-csv-1.19.2.jar                 flink-sql-gateway-api-1.19.2.jar            log4j-core-2.17.1.jar
flink-dist-1.19.2.jar                flink-table-api-java-uber-1.19.2.jar        log4j-slf4j-impl-2.17.1.jar
flink-json-1.19.2.jar                flink-table-planner-loader-1.19.2.jar       postgresql-42.6.0.jar

```



- docker ps total exec for jobmanager :

```
PS C:\Users\ashfa\Downloads> docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED       STATUS          PORTS                                                NAMES
d985abfb6e0c   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   9 hours ago   Up 48 minutes   6123/tcp, 8081/tcp                                   docker-taskmanager-1
3b076c09872f   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   9 hours ago   Up 48 minutes   6123/tcp, 8081/tcp                                   docker-taskmanager-2
fc1613c83355   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   9 hours ago   Up 48 minutes   6123/tcp, 0.0.0.0:8081->8081/tcp                     docker-jobmanager-1
e127e390f64a   grafana/grafana:latest          "/run.sh"                9 hours ago   Up 48 minutes   0.0.0.0:3000->3000/tcp                               docker-grafana-1
ff40462d2453   wurstmeister/kafka:latest       "start-kafka.sh"         9 hours ago   Up 48 minutes   0.0.0.0:9092->9092/tcp, 9093/tcp                     docker-kafka-1
fcde729350b8   mongo:latest                    "docker-entrypoint.s…"   9 hours ago   Up 48 minutes   0.0.0.0:27017->27017/tcp                             docker-mongo-1
24fadebe48b0   postgres:latest                 "docker-entrypoint.s…"   9 hours ago   Up 48 minutes   0.0.0.0:5432->5432/tcp                               docker-postgres-1
74450a0b585c   wurstmeister/zookeeper:latest   "/bin/sh -c '/usr/sb…"   9 hours ago   Up 48 minutes   22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   docker-zookeeper-1
PS C:\Users\ashfa\Downloads> docker cp .\flink-sql-gateway-api-1.19.2.jar fc1613c83355:/opt/flink/lib/
Successfully copied 40.4kB to fc1613c83355:/opt/flink/lib/
PS C:\Users\ashfa\Downloads> docker cp .\flink-sql-gateway-api-1.19.2.jar fc1613c83355:/opt/flink/lib/^C
PS C:\Users\ashfa\Downloads> docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED              STATUS              PORTS                                                      NAMES
169042a24074   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   About a minute ago   Up 59 seconds       6123/tcp, 8081/tcp                                         docker-taskmanager-1
8346def650a9   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   About a minute ago   Up 59 seconds       6123/tcp, 8081/tcp                                         docker-taskmanager-2
f9b03c3c870f   apache/flink:1.19.2-java17      "/docker-entrypoint.…"   About a minute ago   Up About a minute   0.0.0.0:8081->8081/tcp, 6123/tcp, 0.0.0.0:8083->8083/tcp   docker-jobmanager-1
33fb7a4b85c8   postgres:latest                 "docker-entrypoint.s…"   About a minute ago   Up About a minute   0.0.0.0:5432->5432/tcp                                     docker-postgres-1
f1049a9d0587   mongo:latest                    "docker-entrypoint.s…"   About a minute ago   Up About a minute   0.0.0.0:27017->27017/tcp                                   docker-mongo-1
868bc0346211   wurstmeister/kafka:latest       "start-kafka.sh"         About a minute ago   Up About a minute   0.0.0.0:9092->9092/tcp, 9093/tcp                           docker-kafka-1
861681779ec6   wurstmeister/zookeeper:latest   "/bin/sh -c '/usr/sb…"   About a minute ago   Up About a minute   22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp         docker-zookeeper-1
e127e390f64a   grafana/grafana:latest          "/run.sh"                10 hours ago         Up About an hour    0.0.0.0:3000->3000/tcp                                     docker-grafana-1
PS C:\Users\ashfa\Downloads> docker exec -it f9b /bin/bash
root@f9b03c3c870f:/opt/flink# ls
bin  conf  custom-lib  examples  lib  LICENSE  licenses  log  NOTICE  opt  plugins  README.txt
root@f9b03c3c870f:/opt/flink# cd custom-lib/
root@f9b03c3c870f:/opt/flink/custom-lib# ls
flink-connector-jdbc-3.3.0-1.19.jar       flink-sql-connector-mongodb-1.2.0-1.19.jar
flink-sql-connector-kafka-3.3.0-1.19.jar  postgresql-42.6.0.jar
root@f9b03c3c870f:/opt/flink/custom-lib# cd ..
root@f9b03c3c870f:/opt/flink# cd li
bash: cd: li: No such file or directory
root@f9b03c3c870f:/opt/flink# cd lib
root@f9b03c3c870f:/opt/flink/lib# ls
flink-cep-1.19.2.jar              flink-json-1.19.2.jar                  flink-table-runtime-1.19.2.jar  log4j-slf4j-impl-2.17.1.jar
flink-connector-files-1.19.2.jar  flink-scala_2.12-1.19.2.jar            log4j-1.2-api-2.17.1.jar
flink-csv-1.19.2.jar              flink-table-api-java-uber-1.19.2.jar   log4j-api-2.17.1.jar
flink-dist-1.19.2.jar             flink-table-planner-loader-1.19.2.jar  log4j-core-2.17.1.jar
root@f9b03c3c870f:/opt/flink/lib# ls
flink-cep-1.19.2.jar              flink-json-1.19.2.jar                 flink-table-planner-loader-1.19.2.jar  log4j-core-2.17.1.jar
flink-connector-files-1.19.2.jar  flink-scala_2.12-1.19.2.jar           flink-table-runtime-1.19.2.jar         log4j-slf4j-impl-2.17.1.jar
flink-csv-1.19.2.jar              flink-sql-gateway-api-1.19.2.jar      log4j-1.2-api-2.17.1.jar
flink-dist-1.19.2.jar             flink-table-api-java-uber-1.19.2.jar  log4j-api-2.17.1.jar
root@f9b03c3c870f:/opt/flink/lib# cd ../conf
root@f9b03c3c870f:/opt/flink/conf# ls
config.yaml           log4j-console.properties  log4j-session.properties  logback-session.xml  masters  zoo.cfg
log4j-cli.properties  log4j.properties          logback-console.xml       logback.xml          workers
root@f9b03c3c870f:/opt/flink/conf#  mv /opt/flink/custom-lib/*.jar /opt/flink/lib/
root@f9b03c3c870f:/opt/flink/conf# ls ../lib
flink-cep-1.19.2.jar                 flink-scala_2.12-1.19.2.jar                 flink-table-runtime-1.19.2.jar
flink-connector-files-1.19.2.jar     flink-sql-connector-kafka-3.3.0-1.19.jar    log4j-1.2-api-2.17.1.jar
flink-connector-jdbc-3.3.0-1.19.jar  flink-sql-connector-mongodb-1.2.0-1.19.jar  log4j-api-2.17.1.jar
flink-csv-1.19.2.jar                 flink-sql-gateway-api-1.19.2.jar            log4j-core-2.17.1.jar
flink-dist-1.19.2.jar                flink-table-api-java-uber-1.19.2.jar        log4j-slf4j-impl-2.17.1.jar
flink-json-1.19.2.jar                flink-table-planner-loader-1.19.2.jar       postgresql-42.6.0.jar
root@f9b03c3c870f:/opt/flink/conf# ./bin/sql-gateway.sh start-foreground -Dsql-gateway.endpoint.rest.address=0.0.0.0 -Dsql-gateway.endpoint.rest.port=8083^C
root@f9b03c3c870f:/opt/flink/conf# cd ..
root@f9b03c3c870f:/opt/flink# ls
bin  conf  custom-lib  examples  lib  LICENSE  licenses  log  NOTICE  opt  plugins  README.txt



root@f9b03c3c870f:/opt/flink# ./bin/sql-gateway.sh start-foreground -Dsql-gateway.endpoint.rest.address=0.0.0.0 -Dsql-gateway.endpoint.rest.port=8083


IMPORTANT

Note this above command was opt1 we can also add the sql gateway config in
file conf/config.yaml:

sql-gateway.endpoint.rest.address: 0.0.0.0
sql-gateway.endpoint.rest.port: 8083




start as 

./bin/sql-gateway.sh start-foreground.





Starting sql-gateway as a console application on host f9b03c3c870f.
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
WARNING: Unknown module: jdk.compiler specified to --add-exports
2025-05-09 02:29:13,675 INFO  org.apache.flink.table.gateway.SqlGateway                    [] - --------------------------------------------------------------------------------
2025-05-09 02:29:13,676 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  Starting SqlGateway (Version: 1.19.2, Scala: 2.12, Rev:7d9f0e0, Date:2025-01-27T23:11:12+01:00)
2025-05-09 02:29:13,676 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  OS current user: root
2025-05-09 02:29:13,677 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  Current Hadoop/Kerberos user: <no hadoop dependency found>
2025-05-09 02:29:13,677 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  JVM: OpenJDK 64-Bit Server VM - Eclipse Adoptium - 17/17.0.14+7
2025-05-09 02:29:13,677 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  Arch: amd64
2025-05-09 02:29:13,677 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  Maximum heap size: 1954 MiBytes
2025-05-09 02:29:13,677 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  JAVA_HOME: /opt/java/openjdk
2025-05-09 02:29:13,677 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  No Hadoop Dependency available
2025-05-09 02:29:13,678 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  JVM Options:
2025-05-09 02:29:13,678 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -XX:+IgnoreUnrecognizedVMOptions
2025-05-09 02:29:13,678 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=java.base/sun.net.util=ALL-UNNAMED
2025-05-09 02:29:13,678 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=java.rmi/sun.rmi.registry=ALL-UNNAMED
2025-05-09 02:29:13,678 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.lang=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.net=ALL-UNNAMED
2025-05-09 02:29:13,679 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.io=ALL-UNNAMED
2025-05-09 02:29:13,680 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.nio=ALL-UNNAMED
2025-05-09 02:29:13,680 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/sun.nio.ch=ALL-UNNAMED
2025-05-09 02:29:13,680 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
2025-05-09 02:29:13,680 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.text=ALL-UNNAMED
2025-05-09 02:29:13,680 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.time=ALL-UNNAMED
2025-05-09 02:29:13,680 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.util=ALL-UNNAMED
2025-05-09 02:29:13,681 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED
2025-05-09 02:29:13,681 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED
2025-05-09 02:29:13,681 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED
2025-05-09 02:29:13,681 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -Dlog.file=/opt/flink/log/flink--sql-gateway-0-f9b03c3c870f.log
2025-05-09 02:29:13,681 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -Dlog4j.configuration=file:/opt/flink/conf/log4j-console.properties
2025-05-09 02:29:13,682 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -Dlog4j.configurationFile=file:/opt/flink/conf/log4j-console.properties
2025-05-09 02:29:13,682 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -Dlogback.configurationFile=file:/opt/flink/conf/logback-console.xml
2025-05-09 02:29:13,682 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  Program Arguments:
2025-05-09 02:29:13,683 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -Dsql-gateway.endpoint.rest.address=0.0.0.0
2025-05-09 02:29:13,683 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -     -Dsql-gateway.endpoint.rest.port=8083
2025-05-09 02:29:13,683 INFO  org.apache.flink.table.gateway.SqlGateway                    [] -  Classpath: /opt/flink/lib/flink-cep-1.19.2.jar:/opt/flink/lib/flink-connector-files-1.19.2.jar:/opt/flink/lib/flink-connector-jdbc-3.3.0-1.19.jar:/opt/flink/lib/flink-csv-1.19.2.jar:/opt/flink/lib/flink-json-1.19.2.jar:/opt/flink/lib/flink-scala_2.12-1.19.2.jar:/opt/flink/lib/flink-sql-connector-kafka-3.3.0-1.19.jar:/opt/flink/lib/flink-sql-connector-mongodb-1.2.0-1.19.jar:/opt/flink/lib/flink-sql-gateway-api-1.19.2.jar:/opt/flink/lib/flink-table-api-java-uber-1.19.2.jar:/opt/flink/lib/flink-table-planner-loader-1.19.2.jar:/opt/flink/lib/flink-table-runtime-1.19.2.jar:/opt/flink/lib/log4j-1.2-api-2.17.1.jar:/opt/flink/lib/log4j-api-2.17.1.jar:/opt/flink/lib/log4j-core-2.17.1.jar:/opt/flink/lib/log4j-slf4j-impl-2.17.1.jar:/opt/flink/lib/postgresql-42.6.0.jar:/opt/flink/lib/flink-dist-1.19.2.jar:/opt/flink/opt/flink-sql-gateway-1.19.2.jar:/opt/flink/opt/flink-python-1.19.2.jar:::
2025-05-09 02:29:13,683 INFO  org.apache.flink.table.gateway.SqlGateway                    [] - --------------------------------------------------------------------------------
2025-05-09 02:29:13,684 INFO  org.apache.flink.table.gateway.SqlGateway                    [] - Registered UNIX signal handlers for [TERM, HUP, INT]
2025-05-09 02:29:13,691 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Using standard YAML parser to load flink configuration file from /opt/flink/conf/config.yaml.
2025-05-09 02:29:13,720 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: blob.server.port, 6124
2025-05-09 02:29:13,720 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: taskmanager.memory.process.size, 1728m
2025-05-09 02:29:13,720 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: taskmanager.bind-host, 0.0.0.0
2025-05-09 02:29:13,720 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: jobmanager.execution.failover-strategy, region
2025-05-09 02:29:13,720 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: jobmanager.rpc.address, jobmanager
2025-05-09 02:29:13,721 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: jobmanager.memory.process.size, 1600m
2025-05-09 02:29:13,721 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: jobmanager.rpc.port, 6123
2025-05-09 02:29:13,721 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: query.server.port, 6125
2025-05-09 02:29:13,721 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: rest.bind-address, 0.0.0.0
2025-05-09 02:29:13,721 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: jobmanager.bind-host, 0.0.0.0
2025-05-09 02:29:13,722 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: parallelism.default, 1
2025-05-09 02:29:13,722 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: taskmanager.numberOfTaskSlots, 1
2025-05-09 02:29:13,722 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: rest.address, 0.0.0.0
2025-05-09 02:29:13,722 INFO  org.apache.flink.configuration.GlobalConfiguration           [] - Loading configuration property: env.java.opts.all, --add-exports=java.base/sun.net.util=ALL-UNNAMED --add-exports=java.rmi/sun.rmi.registry=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED
2025-05-09 02:29:13,744 INFO  org.apache.flink.client.cli.CliFrontend                      [] - Loading FallbackYarnSessionCli
2025-05-09 02:29:13,758 INFO  org.apache.flink.core.fs.FileSystem                          [] - Hadoop is not in the classpath/dependencies. The extended set of supported File Systems via Hadoop is not available.
2025-05-09 02:29:13,767 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: external-resource-gpu
2025-05-09 02:29:13,770 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-prometheus
2025-05-09 02:29:13,771 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-datadog
2025-05-09 02:29:13,771 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-graphite
2025-05-09 02:29:13,771 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-jmx
2025-05-09 02:29:13,771 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-influx
2025-05-09 02:29:13,772 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-slf4j
2025-05-09 02:29:13,773 INFO  org.apache.flink.core.plugin.DefaultPluginManager            [] - Plugin loader with ID not found, creating it: metrics-statsd
2025-05-09 02:29:13,793 INFO  org.apache.flink.table.gateway.service.context.DefaultContext [] - Execution config: {execution.savepoint.ignore-unclaimed-state=false, execution.attached=true, execution.savepoint-restore-mode=NO_CLAIM, execution.shutdown-on-attached-exit=false, pipeline.jars=[], pipeline.classpaths=[], execution.target=remote}
2025-05-09 02:29:13,796 INFO  org.apache.flink.table.gateway.api.utils.ThreadUtils         [] - Created thread pool sql-gateway-operation-pool with core size 5, max size 500 and keep alive time 300000ms.
2025-05-09 02:29:14,080 INFO  org.apache.flink.configuration.Configuration                 [] - Config uses fallback configuration key 'rest.port' instead of key 'rest.bind-port'
2025-05-09 02:29:14,095 INFO  org.apache.flink.table.gateway.rest.SqlGatewayRestEndpoint   [] - Upload directory /tmp/flink-web-upload does not exist.
2025-05-09 02:29:14,096 INFO  org.apache.flink.table.gateway.rest.SqlGatewayRestEndpoint   [] - Created directory /tmp/flink-web-upload for file uploads.
2025-05-09 02:29:14,098 INFO  org.apache.flink.table.gateway.rest.SqlGatewayRestEndpoint   [] - Starting rest endpoint.
2025-05-09 02:29:14,271 INFO  org.apache.flink.table.gateway.rest.SqlGatewayRestEndpoint   [] - Rest endpoint listening at 0.0.0.0:8083





```

- verification 

```

PS C:\Users\ashfa\Downloads> curl http://localhost:8083/v1/info


StatusCode        : 200
StatusDescription : OK
Content           : {"productName":"Apache Flink","version":"1.19.2"}
RawContent        : HTTP/1.1 200 OK
                    access-control-allow-origin: *
                    connection: keep-alive
                    Content-Length: 49
                    Content-Type: application/json; charset=UTF-8

                    {"productName":"Apache Flink","version":"1.19.2"}
Forms             : {}
Headers           : {[access-control-allow-origin, *], [connection, keep-alive], [Content-Length, 49], [Content-Type, application/json; charset=UTF-8]}
Images            : {}
InputFields       : {}
Links             : {}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : 49



PS C:\Users\ashfa\Downloads>
```



- NOTE Configuring SQL Gateway in Config.y

```
Current : 
 cat config.yaml
blob:
  server:
    port: '6124'
taskmanager:
  memory:
    process:
      size: 1728m
  bind-host: 0.0.0.0
  numberOfTaskSlots: 1
jobmanager:
  execution:
    failover-strategy: region
  rpc:
    address: jobmanager
    port: 6123
  memory:
    process:
      size: 1600m
  bind-host: 0.0.0.0
query:
  server:
    port: '6125'
rest:
  bind-address: 0.0.0.0
  address: 0.0.0.0
parallelism:
  default: 1
env:
  java:
    opts:
      all: --add-exports=java.base/sun.net.util=ALL-UNNAMED --add-exports=java.rmi/sun.rmi.registry=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED
root@f9b03c3c870f:/opt/flink/conf#



- minimum sql-gateway config

sql-gateway.endpoint.rest.address: 0.0.0.0
sql-gateway.endpoint.rest.port: 8083

- full sql-gateway config

sql-gateway.endpoint.rest.address: 0.0.0.0
sql-gateway.endpoint.rest.port: 8083
execution.planner: blink
execution.type: streaming
execution.result-mode: tableau
execution.parallelism: 1
```


- Tried

- creating a sql-gateway-defaults.yaml in conf folder 
but no use for now as the sql gateway config is referenced from config.yaml.



