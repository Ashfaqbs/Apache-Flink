package com.example.ai.retail.job;

import com.example.ai.retail.dto.SaleEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

/**
 * Kafka deserialization schema for SaleEvent objects.
 * Reads raw JSON bytes from the sale-events topic and converts them
 * to SaleEvent instances for the SalesVelocityAgent.
 */
public class SaleEventDeserializationSchema implements DeserializationSchema<SaleEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // Ignore extra fields from the POS or e-commerce platform event schema
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public SaleEvent deserialize(byte[] message) throws IOException {
        return MAPPER.readValue(message, SaleEvent.class);
    }

    // Streaming source — never signals end of stream
    @Override
    public boolean isEndOfStream(SaleEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<SaleEvent> getProducedType() {
        return TypeInformation.of(SaleEvent.class);
    }
}
