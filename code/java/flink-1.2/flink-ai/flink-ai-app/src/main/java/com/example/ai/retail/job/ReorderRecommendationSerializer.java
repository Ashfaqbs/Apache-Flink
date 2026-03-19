package com.example.ai.retail.job;

import com.example.ai.retail.dto.ReorderRecommendation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

/**
 * Kafka serialization schema for ReorderRecommendation objects.
 * Converts ReorderRecommendation instances to JSON bytes for writing
 * to the reorder-recommendations topic.
 */
public class ReorderRecommendationSerializer implements SerializationSchema<ReorderRecommendation> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(ReorderRecommendation element) {
        try {
            return MAPPER.writeValueAsBytes(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ReorderRecommendation: " + element, e);
        }
    }
}
