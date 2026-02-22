package com.example.ai.workflowagent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyEvent {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("timestamp")
    private String timestamp;

    public MyEvent() {} // MANDATORY FOR FLINK/KRYO SERIALIZATION

    public MyEvent(int id, String name, String timestamp) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "MyEvent{id=" + id + ", name='" + name + "', timestamp='" + timestamp + "'}";
    }
}