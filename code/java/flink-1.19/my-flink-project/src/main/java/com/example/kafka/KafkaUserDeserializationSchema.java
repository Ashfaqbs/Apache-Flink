package com.example.kafka;


import com.example.dto.UserPlain;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import java.io.IOException;

public class KafkaUserDeserializationSchema implements DeserializationSchema<UserPlain> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserPlain deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, UserPlain.class);
    }

    @Override
    public boolean isEndOfStream(UserPlain nextElement) {
        return false;
    }

    @Override
    public TypeInformation<UserPlain> getProducedType() {
        return TypeInformation.of(UserPlain.class);
    }
}
