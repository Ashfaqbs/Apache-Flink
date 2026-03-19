package com.example.ai.retail.job;

import com.example.ai.retail.dto.ChurnAlert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

/**
 * Kafka serialization schema for ChurnAlert objects.
 * Converts ChurnAlert instances to JSON bytes for writing to the churn-alerts topic.
 */
public class ChurnAlertSerializer implements SerializationSchema<ChurnAlert> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(ChurnAlert element) {
        try {
            return MAPPER.writeValueAsBytes(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ChurnAlert: " + element, e);
        }
    }
}
