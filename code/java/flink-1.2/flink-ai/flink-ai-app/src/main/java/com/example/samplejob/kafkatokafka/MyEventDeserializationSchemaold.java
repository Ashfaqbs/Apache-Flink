package com.example.samplejob.kafkatokafka;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class MyEventDeserializationSchemaold implements DeserializationSchema<MyEvent2> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public MyEvent2 deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, MyEvent2.class);
    }

    @Override
    public boolean isEndOfStream(MyEvent2 nextElement) {
        return false;
    }

    @Override
    public TypeInformation<MyEvent2> getProducedType() {
        return TypeInformation.of(MyEvent2.class);
    }
}