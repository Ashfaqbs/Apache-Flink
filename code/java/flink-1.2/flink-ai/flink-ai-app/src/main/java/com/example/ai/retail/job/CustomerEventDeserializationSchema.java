package com.example.ai.retail.job;

import com.example.ai.retail.dto.CustomerEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

/**
 * Kafka deserialization schema for CustomerEvent objects.
 * Reads raw JSON bytes from the customer-events topic and converts them
 * to CustomerEvent instances for the ChurnDetectionAgent.
 */
public class CustomerEventDeserializationSchema implements DeserializationSchema<CustomerEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // Tolerate additional fields that upstream event producers may include
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public CustomerEvent deserialize(byte[] message) throws IOException {
        return MAPPER.readValue(message, CustomerEvent.class);
    }

    // Streaming source — never signals end of stream
    @Override
    public boolean isEndOfStream(CustomerEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<CustomerEvent> getProducedType() {
        return TypeInformation.of(CustomerEvent.class);
    }
}
