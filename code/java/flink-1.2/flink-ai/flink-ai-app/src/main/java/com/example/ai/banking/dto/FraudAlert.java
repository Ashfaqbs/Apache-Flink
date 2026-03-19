package com.example.ai.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Output of the FraudDetectionAgent. Represents the fraud analysis result
 * for a single transaction and is written to the fraud-alerts Kafka topic.
 */
public class FraudAlert {

    @JsonProperty("transactionId")
    private final String transactionId;

    @JsonProperty("accountId")
    private final String accountId;

    // Severity level: LOW, MEDIUM, HIGH, or CRITICAL
    @JsonProperty("riskLevel")
    private final String riskLevel;

    // Specific signals that triggered the risk classification
    @JsonProperty("fraudSignals")
    private final List<String> fraudSignals;

    // Description of what was done (e.g., "card_blocked", "alert_sent", "monitoring")
    @JsonProperty("actionTaken")
    private final String actionTaken;

    @JsonProperty("timestamp")
    private final String timestamp;

    // No-arg constructor required by Jackson for deserialization
    public FraudAlert() {
        this.transactionId = null;
        this.accountId = null;
        this.riskLevel = null;
        this.fraudSignals = null;
        this.actionTaken = null;
        this.timestamp = null;
    }

    public FraudAlert(
            String transactionId,
            String accountId,
            String riskLevel,
            List<String> fraudSignals,
            String actionTaken,
            String timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.riskLevel = riskLevel;
        this.fraudSignals = fraudSignals;
        this.actionTaken = actionTaken;
        this.timestamp = timestamp;
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public String getRiskLevel() { return riskLevel; }
    public List<String> getFraudSignals() { return fraudSignals; }
    public String getActionTaken() { return actionTaken; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "FraudAlert{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", fraudSignals=" + fraudSignals +
                ", actionTaken='" + actionTaken + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
