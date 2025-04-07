package com.example.kafka;
import com.example.dto.MyEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import java.io.IOException;

public class MyEventDeserializationSchema implements DeserializationSchema<MyEvent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule()); // Register JSR310 module
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Override
    public MyEvent deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, MyEvent.class);
    }

    @Override
    public boolean isEndOfStream(MyEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<MyEvent> getProducedType() {
        return TypeInformation.of(MyEvent.class);
    }
}
