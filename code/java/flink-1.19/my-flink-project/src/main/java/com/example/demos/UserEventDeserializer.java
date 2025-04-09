package com.example.demos;

import com.example.dto.MyEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class UserEventDeserializer implements DeserializationSchema<UserEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

//    static {
//        objectMapper.registerModule(new JavaTimeModule()); // Register JSR310 module
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//    }

    @Override
    public UserEvent deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, UserEvent.class);
    }

    @Override
    public boolean isEndOfStream(UserEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<UserEvent> getProducedType() {
        return TypeInformation.of(UserEvent.class);
    }
}
