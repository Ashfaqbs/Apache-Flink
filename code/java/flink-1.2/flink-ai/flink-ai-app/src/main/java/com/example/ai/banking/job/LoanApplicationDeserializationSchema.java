package com.example.ai.banking.job;

import com.example.ai.banking.dto.LoanApplication;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

/**
 * Kafka deserialization schema for LoanApplication objects.
 * Reads raw JSON bytes from the loan-applications topic and converts them
 * to LoanApplication instances for the LoanRiskExtractionAgent.
 */
public class LoanApplicationDeserializationSchema implements DeserializationSchema<LoanApplication> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // Tolerate new fields from upstream producers without breaking this consumer
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public LoanApplication deserialize(byte[] message) throws IOException {
        return MAPPER.readValue(message, LoanApplication.class);
    }

    // Streaming source — never signals end of stream
    @Override
    public boolean isEndOfStream(LoanApplication nextElement) {
        return false;
    }

    @Override
    public TypeInformation<LoanApplication> getProducedType() {
        return TypeInformation.of(LoanApplication.class);
    }
}
