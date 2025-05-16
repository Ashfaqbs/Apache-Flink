FULL CONTEXT IN  [Analysis file](../MinIO/MIO-FlinkSQL-JOB.txt)

- config 

In jobmanager, taskmanager > opt/flink/conf/config.yaml update and restart the container
- verify the logs
```yaml
blob:
  server:
    port: '6124'
taskmanager:
  memory:
    process:
      size: 1728m
  bind-host: 0.0.0.0
  numberOfTaskSlots: 1

fs:
  s3a:
    impl: org.apache.hadoop.fs.s3a.S3AFileSystem
    endpoint: http://minio:9000
    access:
      key: admin
      secret: admin123
    path:
      style:
        access: true
    

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

```
- cp jars 

```
cp /opt/flink/opt/flink-s3-fs-hadoop-1.19.2.jar /opt/flink/plugins/s3-fs-hadoop/

root@c0e63e44821f:/opt/flink#  ls /opt/flink/plugins/s3-fs-hadoop/
flink-s3-fs-hadoop-1.19.2.jar
```



- sql job 

```


Flink SQL> 
CREATE TABLE minio_sink (
    id INT,
    name STRING
  ) WITH (
    'connector' = 'filesystem',
    'path' = 's3a://my-bucket-2/',
    'format' = 'csv'
  );
[INFO] Execute statement succeed.

Flink SQL> INSERT INTO minio_sink VALUES (1, 'Alice'), (2, 'Bob');
[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: 631713b39c78694d1f35fc3ba1127798

```

- Error :
```
2025-05-16 09:16:49
org.apache.flink.runtime.JobException: Recovery is suppressed by NoRestartBackoffTimeStrategy
	at org.apache.flink.runtime.executiongraph.failover.ExecutionFailureHandler.handleFailure(ExecutionFailureHandler.java:180)
	at org.apache.flink.runtime.executiongraph.failover.ExecutionFailureHandler.getFailureHandlingResult(ExecutionFailureHandler.java:107)
	at org.apache.flink.runtime.scheduler.DefaultScheduler.recordTaskFailure(DefaultScheduler.java:277)
	at org.apache.flink.runtime.scheduler.DefaultScheduler.handleTaskFailure(DefaultScheduler.java:268)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(Unknown Source)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(Unknown Source)
Caused by: org.apache.flink.core.fs.UnsupportedFileSystemSchemeException: Could not find a file system implementation for scheme 's3a'. The scheme is directly supported by Flink through the following plugin(s): flink-s3-fs-hadoop. Please ensure that each plugin resides within its own subfolder within the plugins directory. See https://nightlies.apache.org/flink/flink-docs-stable/docs/deployment/filesystems/plugins/ for more information. If you want to use a Hadoop file system for that scheme, please add the scheme to the configuration fs.allowed-fallback-filesystems. For a full list of supported file systems, please see https://nightlies.apache.org/flink/flink-docs-stable/ops/filesystems/.
	at org.apache.flink.core.fs.FileSystem.getUnguardedFileSystem(FileSystem.java:514)
	at org.apache.flink.core.fs.FileSystem.get(FileSystem.java:408)
	at org.apache.flink.runtime.taskmanager.Task.run(Task.java:566)
	at java.base/java.lang.Thread.run(Unknown Source)

```