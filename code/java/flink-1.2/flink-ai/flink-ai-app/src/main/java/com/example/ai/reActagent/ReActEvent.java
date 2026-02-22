package com.example.ai.reActagent;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReActEvent {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("timestamp")
    private String timestamp;

    public ReActEvent() {} // Default constructor for Flink/Jackson

    public ReActEvent(int id, String name, String timestamp) {
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
        return "ReActEvent{id=" + id + ", name='" + name + "', timestamp='" + timestamp + "'}";
    }
}