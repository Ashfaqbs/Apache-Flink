package com.example.kafka;

import com.example.dto.MyEvent;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.SerializationSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyEventSerializer implements SerializationSchema<MyEvent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        // Register the module to handle Java 8 date/time types
        objectMapper.registerModule(new JavaTimeModule());
    }
    @Override
    public byte[] serialize(MyEvent event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing MyEvent", e);
        }
    }
}
