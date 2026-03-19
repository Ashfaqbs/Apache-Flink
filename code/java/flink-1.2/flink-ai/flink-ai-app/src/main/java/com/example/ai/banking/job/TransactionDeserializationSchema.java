package com.example.ai.banking.job;

import com.example.ai.banking.dto.Transaction;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

/**
 * Kafka deserialization schema for Transaction objects.
 * Reads raw JSON bytes from the bank-transactions topic and converts them
 * to Transaction instances for the FraudDetectionAgent.
 */
public class TransactionDeserializationSchema implements DeserializationSchema<Transaction> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // Ignore extra fields in case the Kafka producer adds new fields —
        // we only map what Transaction declares
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Transaction deserialize(byte[] message) throws IOException {
        return MAPPER.readValue(message, Transaction.class);
    }

    // Return false always — this is a streaming source with no defined end
    @Override
    public boolean isEndOfStream(Transaction nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Transaction> getProducedType() {
        return TypeInformation.of(Transaction.class);
    }
}
