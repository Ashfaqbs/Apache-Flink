package com.example.ai.reviewpipeline.job;

import com.example.ai.reviewpipeline.dto.ProductSuggestion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

/** Serializes ProductSuggestion to JSON bytes for the Kafka sink. */
public class ProductSuggestionSerializer implements SerializationSchema<ProductSuggestion> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(ProductSuggestion element) {
        try {
            return MAPPER.writeValueAsBytes(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ProductSuggestion", e);
        }
    }
}
