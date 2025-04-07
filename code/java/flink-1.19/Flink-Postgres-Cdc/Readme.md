# Flink SQL CDC Streaming with PostgreSQL

This guide sets up a working example of using Apache Flink SQL (v1.19) with PostgreSQL CDC (Change Data Capture) using Debezium inside a Docker environment.

---

## 🌐 Project Goal

Stream real-time changes from a PostgreSQL database into Flink SQL using the Flink SQL Gateway and observe them in real-time using CDC.

---

## 🧠 Key Concepts

- **CDC (Change Data Capture)**: Monitors and streams data changes (INSERT, UPDATE, DELETE) from source systems.
- **Debezium**: An open-source platform for CDC that monitors databases and emits changes to Kafka, Flink, etc.
- **Flink SQL**: Enables streaming queries using SQL semantics.

---

## 📁 Project Structure

```
flink-postgres-cdc/
├── docker-compose.yml
├── init.sql
├── flink/
│   ├── lib/
│   │   └── (JARs copied automatically)
│   └── Dockerfile
```

---

## 🐳 Docker Compose File

```yaml
yaml
version: '3.8'

services:
  postgres:
    image: debezium/postgres:latest
    container_name: pg-cdc
    restart: always
    environment:
      POSTGRES_USER: flinkuser
      POSTGRES_PASSWORD: flinkpw
      POSTGRES_DB: flinkdb
    ports:
      - "5432:5432"
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  sql-gateway:
    build: ./flink
    container_name: flink-sql-gateway
    ports:
      - "8083:8083"
    depends_on:
      - postgres
```

---

## 🔢 `init.sql`

```sql
CREATE TABLE customers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100)
);

INSERT INTO customers (name, email)
VALUES
  ('Alice', 'alice@example.com'),
  ('Bob', 'bob@example.com');
```

---

## 🐳 Flink Dockerfile (for SQL Gateway)

```Dockerfile
FROM flink:1.19-scala_2.12

# Add Flink SQL Gateway jar and Flink SQL Connector for PostgreSQL CDC
RUN mkdir -p /opt/flink/lib

# Copy the JARs (use ADD to trigger rebuilds when new JARs added)
ADD https://repo1.maven.org/maven2/org/apache/flink/flink-sql-gateway/1.19.0/flink-sql-gateway-1.19.0.jar /opt/flink/lib/
ADD https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-postgres-cdc/3.0.1/flink-sql-connector-postgres-cdc-3.0.1.jar /opt/flink/lib/

EXPOSE 8083

CMD ["bin/sql-gateway.sh", "start-foreground"]
```

---

## ▶️ Steps to Run

```bash
git clone <repo>
cd flink-postgres-cdc
docker-compose up --build
```

After a few seconds:
- PostgreSQL is up with initial data
- Flink SQL Gateway is listening on port `8083`

---

## 🧪 Run CDC Streaming Queries

### Connect to SQL Gateway (using Flink CLI or REST API)
You can exec into the container:

```bash
docker exec -it flink-sql-gateway ./bin/sql-client.sh
```

### Create CDC Source Table

```sql
CREATE TABLE customers_src (
  id INT,
  name STRING,
  email STRING,
  PRIMARY KEY (id) NOT ENFORCED
) WITH (
  'connector' = 'postgres-cdc',
  'hostname' = 'postgres',
  'port' = '5432',
  'username' = 'flinkuser',
  'password' = 'flinkpw',
  'database-name' = 'flinkdb',
  'schema-name' = 'public',
  'table-name' = 'customers'
);
```

### Run a SELECT Query

```sql
SELECT * FROM customers_src;
```

Now make an insert in the host using `psql` or pgAdmin:

```sql
INSERT INTO customers (name, email) VALUES ('Charlie', 'charlie@example.com');
```

You’ll see the new row in real time in Flink output.

---

## ⚠️ Observations & Enhancements

- Supports only `INSERT` by default in basic query (need sink to consume `UPDATE/DELETE` correctly)
- Add custom sink like Kafka, Elasticsearch, or another Postgres for full ETL pipeline
- You can also auto-connect using REST API to define jobs

---

## 📚 References

- [Apache Flink SQL Gateway](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/sqlclient/)
- [Debezium PostgreSQL Docs](https://debezium.io/documentation/reference/stable/connectors/postgresql.html)
- [Flink CDC Connectors](https://nightlies.apache.org/flink/flink-cdc-docs-release-3.3/docs/connectors/flink-sources/postgres-cdc/)

