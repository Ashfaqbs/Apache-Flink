# 🚀 ZooKeeper in Apache Flink: Deployment Guide

---

## ✅ 1. What is ZooKeeper’s role in Flink?

ZooKeeper is **only needed** when you enable **High Availability (HA)** in Flink.

### 🧠 ZooKeeper handles:

| Responsibility            | What it means                                                          |
| ------------------------- | ---------------------------------------------------------------------- |
| **Leader Election**       | Picks one active JobManager among multiple standby JMs                 |
| **Metadata Coordination** | Keeps track of running job’s status + checkpoints                      |
| **Cluster Coordination**  | Lets TaskManagers connect to the current active JM even after failover |

> ZooKeeper does **not run Flink jobs** or handle data.
> It just acts as a **coordinator + referee** for the Flink HA setup.

---

## ✅ 2. What is the minimum config to enable HA with ZooKeeper?

You must provide these in `flink-conf.yaml`:

```yaml
high-availability: zookeeper

high-availability.storageDir: hdfs:///flink/ha/

high-availability.zookeeper.quorum: zk1:2181,zk2:2181,zk3:2181
```

| Config             | Why it’s needed                        |
| ------------------ | -------------------------------------- |
| `type: zookeeper`  | Enables HA mode                        |
| `storageDir`       | Stores job metadata + checkpoints      |
| `zookeeper.quorum` | Connects to ZooKeeper for coordination |

---

## ✅ 3. What is the ideal config (HA + ZK)?

Here’s the full recommended block:

```yaml
high-availability: zookeeper

high-availability.storageDir: hdfs:///flink/ha/

high-availability.zookeeper.quorum: zk1:2181,zk2:2181,zk3:2181

high-availability.zookeeper.client.acl: open
```

* Use **3 ZooKeeper nodes** for quorum
* Use **shared storage** like HDFS, S3, NFS for `storageDir`
* Keep ACL as `open` unless you need Kerberos

---

## ✅ 4. Where should I deploy ZooKeeper?

| Option                                 | Recommended?      | Notes                                            |
| -------------------------------------- | ----------------- | ------------------------------------------------ |
| On same machine as JM or TM            | ❌ Not recommended | One failure brings down both                     |
| On separate dedicated nodes            | ✅ Yes             | Ideal — 3 separate lightweight VMs or containers |
| In Kubernetes                          | ✅ Yes             | Use a `StatefulSet` for ZK pods                  |
| Using cloud-managed ZK (e.g. AWS, GCP) | ✅ If available    | Less ops work for you                            |

> ZooKeeper is **very lightweight** — needs little RAM or CPU
> But it should **not share resources** with heavy processes like Flink

---

## ✅ 5. Why is ZooKeeper not included in Flink binaries?

* Flink is built to **support external ZooKeeper**, not embed it
* ZooKeeper is its **own separate service**
* Flink respects the **Unix philosophy**: 1 service = 1 responsibility
* It avoids bundling ZK to let you **scale, secure, and manage it independently**

To install ZooKeeper:

* Download from [zookeeper.apache.org](https://zookeeper.apache.org/)
* Use a Docker image (`bitnami/zookeeper` is popular)
* Or use a cloud-native service like **Amazon MSK**, **GCP ZK**, etc.

---

## ✅ 6. What is the correct stop/start sequence?

Let’s say:

* **ZooKeeper runs on Node-A**
* **JobManager on Node-B**
* **TaskManagers on Node-C, Node-D**

### 🔻 Stopping Sequence

```text
1. Stop Flink jobs (if needed)
2. Stop all TaskManagers
3. Stop JobManager
4. Stop ZooKeeper cluster (last)
```

### 🔼 Starting Sequence

```text
1. Start ZooKeeper cluster first ✅
2. Start JobManager(s) (can be multiple in HA)
3. Start TaskManagers
4. Flink job will auto-resume from HA metadata if checkpointing was enabled
```

> **Why?**
> JobManager needs to talk to ZooKeeper to register and become leader
> TaskManagers need to know who the current leader is
> ZooKeeper must be up **first**

---

## ✅ 7. Other Important Things to Know

| Question                                              | Answer                                         |
| ----------------------------------------------------- | ---------------------------------------------- |
| **Does Flink HA work without checkpoints?**           | 🟡 Partially — job restarts, but state is lost |
| **Do ZooKeeper nodes store job data?**                | ❌ No — they only store small metadata          |
| **Can I use S3 instead of HDFS?**                     | ✅ Yes — for `high-availability.storageDir`     |
| **How many JMs should I run in HA?**                  | 2 or 3 — one active, others standby            |
| **Can I run ZooKeeper inside same Docker pod as JM?** | ❌ No — separate container or pod preferred     |
| **How big should a ZooKeeper node be?**               | 1 CPU, 1–2 GB RAM is enough                    |

---

## ✅ Summary

| Component      | Role                            | Deploy How                        |
| -------------- | ------------------------------- | --------------------------------- |
| ZooKeeper      | Leader election, failover logic | 3 small separate nodes or pods    |
| JobManager     | Active + standby                | 2–3 instances; restartable        |
| TaskManager    | Run the actual tasks            | As many as needed for parallelism |
| Shared Storage | Stores job state + checkpoints  | S3 / HDFS / NFS                   |
