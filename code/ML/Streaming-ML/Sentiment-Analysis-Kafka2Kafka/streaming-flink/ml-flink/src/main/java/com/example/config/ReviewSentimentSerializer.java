package com.example.config;

import com.example.dto.ReviewSentimentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class ReviewSentimentSerializer implements SerializationSchema<ReviewSentimentDTO> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(ReviewSentimentDTO element) {
        try {
            return mapper.writeValueAsBytes(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ReviewSentimentDTO", e);
        }
    }
}

