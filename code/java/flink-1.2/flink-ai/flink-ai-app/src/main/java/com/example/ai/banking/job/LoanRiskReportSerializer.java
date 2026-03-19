package com.example.ai.banking.job;

import com.example.ai.banking.dto.LoanRiskReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

/**
 * Kafka serialization schema for LoanRiskReport objects.
 * Converts LoanRiskReport instances to JSON bytes for writing to the loan-risk-reports topic.
 */
public class LoanRiskReportSerializer implements SerializationSchema<LoanRiskReport> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(LoanRiskReport element) {
        try {
            return MAPPER.writeValueAsBytes(element);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize LoanRiskReport: " + element, e);
        }
    }
}
