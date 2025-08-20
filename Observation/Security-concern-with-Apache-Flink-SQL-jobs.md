A several approaches to prevent sensitive information from being logged:

## 1. Use Configuration to Reduce SQL Logging

Set these Flink configuration properties to reduce verbose SQL logging:

```yaml
# In flink-conf.yaml
log4j2.logger.org.apache.flink.table.planner.plan.optimize.program.FlinkChainedProgram.level = WARN
log4j2.logger.org.apache.flink.table.planner.delegation.PlannerBase.level = WARN
log4j2.logger.org.apache.flink.table.api.internal.TableEnvironmentImpl.level = WARN
```

## 2. Configure Log4j2 to Filter Sensitive Content

Create a custom log4j2 configuration that filters out DDL statements:

```xml
<!-- log4j2.xml -->
<Configuration>
  <Appenders>
    <Console name="Console">
      <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
      <RegexFilter regex=".*CREATE TABLE.*" onMatch="DENY" onMismatch="NEUTRAL"/>
      <RegexFilter regex=".*INSERT INTO.*" onMatch="DENY" onMismatch="NEUTRAL"/>
    </Console>
  </Appenders>
  <Loggers>
    <Root level="INFO">
      <AppenderRef ref="Console"/>
    </Root>
  </Loggers>
</Configuration>
```

## 3. Use Environment Variables for Credentials

Instead of hardcoding credentials in SQL, use environment variables:

```sql
-- Instead of:
CREATE TABLE kafka_source (
  ...
) WITH (
  'connector' = 'kafka',
  'properties.bootstrap.servers' = 'localhost:9092',
  'properties.sasl.jaas.config' = 'org.apache.kafka.common.security.plain.PlainLoginModule required username="user" password="secret";'
);

-- Use:
CREATE TABLE kafka_source (
  ...
) WITH (
  'connector' = 'kafka',
  'properties.bootstrap.servers' = 'localhost:9092',
  'properties.sasl.jaas.config' = '${KAFKA_SASL_CONFIG}'
);
```

## 4. Externalize Configuration

Use Flink's configuration substitution with external property files:

```properties
# credentials.properties
kafka.username=myuser
kafka.password=mypassword
db.url=jdbc:postgresql://localhost:5432/mydb
db.username=dbuser
db.password=dbpassword
```

Then reference in your SQL:
```sql
CREATE TABLE kafka_source (...) WITH (
  'connector' = 'kafka',
  'properties.sasl.jaas.config' = 'org.apache.kafka.common.security.plain.PlainLoginModule required username="${kafka.username}" password="${kafka.password}";'
);
```

## 5. Set Specific Logger Levels

Add these to your Flink configuration to reduce SQL statement logging:

```yaml
# Reduce table API logging
log4j2.logger.org.apache.flink.table.level = WARN

# Reduce Calcite SQL parser logging  
log4j2.logger.org.apache.calcite.level = WARN

# Reduce connector logging that might expose credentials
log4j2.logger.org.apache.flink.connector.level = WARN
```

## 6. Use Secrets Management

For production environments, integrate with secrets management systems:

- **Kubernetes**: Use Kubernetes secrets
- **AWS**: Use AWS Secrets Manager or Parameter Store
- **Azure**: Use Azure Key Vault
- **HashiCorp Vault**: For on-premise deployments

## 7. Runtime Configuration Override

We can also set these programmatically when submitting jobs:

```bash
flink run \
  -D log4j2.logger.org.apache.flink.table.level=WARN \
  -D log4j2.logger.org.apache.calcite.level=WARN \
  your-job.jar
```

The most effective approach is typically a combination of reducing SQL logging verbosity (#1, #5) and externalizing sensitive configuration (#3, #4). This prevents credentials from appearing in both the SQL statements and the logs while maintaining useful debugging information.
