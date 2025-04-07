package com.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class MyEvent {
    private int id;
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    public MyEvent() {} // Default constructor for deserialization

    public MyEvent(int id, String name, Instant timestamp) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public Instant getTimestamp() { return timestamp; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "MyEvent{id=" + id + ", name='" + name + "', timestamp=" + timestamp + '}';
    }
}
