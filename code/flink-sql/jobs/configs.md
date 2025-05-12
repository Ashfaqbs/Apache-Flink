
- Kafka 
```

CREATE TABLE kafka_users (
  name STRING,
  email STRING,
  role STRING
) WITH (
  'connector' = 'kafka',
  'topic' = 'my-topic',
  'properties.bootstrap.servers' = 'kafka:9093',
  'properties.group.id' = 'flink-group',
  'scan.startup.mode' = 'earliest-offset',
  'format' = 'json',
  'json.ignore-parse-errors' = 'true',
  'properties.security.protocol' = 'SSL',
  'properties.ssl.truststore.location' = '/opt/certs/truststore.jks',
  'properties.ssl.truststore.password' = 'truststore_password'
  -- Add below if client authentication is required:
  -- 'properties.ssl.keystore.location' = '/opt/certs/keystore.jks',
  -- 'properties.ssl.keystore.password' = 'keystore_password',
  -- 'properties.ssl.key.password' = 'key_password'
);
```

- DB


```
CREATE TABLE users_sink (
  name STRING,
  email STRING
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:postgresql://postgres:5432/mainschema?sslmode=verify-full&sslrootcert=/opt/certs/ca.pem',
  'table-name' = 'users',
  'username' = 'postgres',
  'password' = 'admin',
  'driver' = 'org.postgresql.Driver'
);
```