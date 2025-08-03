Comparative Table:

To summarize the compatibility, the following table presents the supported versions for each Flink release:


| **Flink Version** | **Java Support**                                | **Python (PyFlink) Support**      |
|-------------------|-------------------------------------------------|-----------------------------------|
| **2.0**           | 17 (recommended), 21 (officially supported), Java 8 deprecated | 3.8, 3.9, 3.10, 3.11             |
| **1.20**          | 11 (stable), 17 (experimental)                  | 3.8, 3.9, 3.10, 3.11             |


Resources:

https://nightlies.apache.org/flink/flink-docs-release-2.0/release-notes/flink-2.0/

https://nightlies.apache.org/flink/flink-docs-release-2.0/release-notes/flink-1.20/

https://nightlies.apache.org/flink/flink-docs-stable/

https://hub.docker.com/_/flink/tags



### Docker Table:

| **JDK Version** | **Flink 2.0.0 Module**                                | **Status**                             |
|-----------------|-------------------------------------------------------|----------------------------------------|
| **Java 11**     | flink:2.0.0-scala_2.12-java11                        | Supported (not recommended)            |
| **Java 17**     | flink:2.0.0-scala_2.12-java17                        | Default and Recommended                |
| **Java 21**     | flink:2.0.0-scala_2.12-java21                        | Experimental Support                   |
