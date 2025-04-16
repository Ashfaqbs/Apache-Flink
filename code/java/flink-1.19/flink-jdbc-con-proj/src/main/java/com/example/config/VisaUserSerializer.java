package com.example.config;
import com.example.dto.VisaUser;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.SerializationSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
public class VisaUserSerializer  implements SerializationSchema<VisaUser>{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        // Register the module to handle Java 8 date/time types
//        objectMapper.registerModule(new JavaTimeModule()); required when using timestamps
    }
    @Override
    public byte[] serialize(VisaUser event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing MyEvent", e);
        }
    }
}
