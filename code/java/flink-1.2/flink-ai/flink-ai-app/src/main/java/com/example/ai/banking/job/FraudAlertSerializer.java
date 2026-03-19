package com.example.ai.banking.job;

import com.example.ai.banking.dto.FraudAlert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

/**
 * Kafka serialization schema for FraudAlert objects.
 * Converts FraudAlert instances to JSON bytes for writing to the fraud-alerts topic.
 */
public class FraudAlertSerializer implements SerializationSchema<FraudAlert> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(FraudAlert element) {
        try {
            return MAPPER.writeValueAsBytes(element);
        } catch (Exception e) {
            // Wrap as unchecked — Flink's SerializationSchema does not declare checked exceptions
            throw new RuntimeException("Failed to serialize FraudAlert: " + element, e);
        }
    }
}
