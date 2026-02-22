package com.example.ai.reActagent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import java.io.IOException;

public class ReActEventDeserializationSchema implements DeserializationSchema<ReActEvent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ReActEvent deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, ReActEvent.class);
    }

    @Override
    public boolean isEndOfStream(ReActEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<ReActEvent> getProducedType() {
        return TypeInformation.of(ReActEvent.class);
    }
}