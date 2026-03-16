package com.example.ai.retail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Output of the ChurnDetectionAgent. Contains the churn risk classification
 * and the signals that triggered it. Written to the churn-alerts Kafka topic
 * for consumption by CRM and retention systems.
 */
public class ChurnAlert {

    @JsonProperty("customerId")
    private final String customerId;

    // Risk level: LOW, MEDIUM, HIGH, or CRITICAL
    @JsonProperty("churnRisk")
    private final String churnRisk;

    // Specific behavioral signals detected by the LLM
    @JsonProperty("churnSignals")
    private final List<String> churnSignals;

    // Description of the automated action taken (e.g., "retention_offer_sent", "escalated_to_agent")
    @JsonProperty("actionTaken")
    private final String actionTaken;

    @JsonProperty("timestamp")
    private final String timestamp;

    // No-arg constructor required by Jackson for deserialization
    public ChurnAlert() {
        this.customerId = null;
        this.churnRisk = null;
        this.churnSignals = null;
        this.actionTaken = null;
        this.timestamp = null;
    }

    public ChurnAlert(
            String customerId,
            String churnRisk,
            List<String> churnSignals,
            String actionTaken,
            String timestamp) {
        this.customerId = customerId;
        this.churnRisk = churnRisk;
        this.churnSignals = churnSignals;
        this.actionTaken = actionTaken;
        this.timestamp = timestamp;
    }

    public String getCustomerId() { return customerId; }
    public String getChurnRisk() { return churnRisk; }
    public List<String> getChurnSignals() { return churnSignals; }
    public String getActionTaken() { return actionTaken; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "ChurnAlert{" +
                "customerId='" + customerId + '\'' +
                ", churnRisk='" + churnRisk + '\'' +
                ", churnSignals=" + churnSignals +
                ", actionTaken='" + actionTaken + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
