package com.example.ai.retail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a behavioral event emitted by the retail platform for a specific customer.
 * Input to the ChurnDetectionAgent for real-time churn risk assessment.
 */
public class CustomerEvent {

    @JsonProperty("customerId")
    private final String customerId;

    // Event type: LOGIN, PURCHASE, BROWSE, SUPPORT_TICKET, INACTIVITY, CART_ABANDON
    @JsonProperty("eventType")
    private final String eventType;

    // Number of sessions in the current period — declining sessions signal disengagement
    @JsonProperty("sessionCount")
    private final int sessionCount;

    // Days since the customer last completed a purchase — key churn predictor
    @JsonProperty("daysSinceLastPurchase")
    private final int daysSinceLastPurchase;

    // Cumulative lifetime spend — high-value customers warrant more aggressive retention
    @JsonProperty("totalSpend")
    private final double totalSpend;

    @JsonProperty("timestamp")
    private final String timestamp;

    // No-arg constructor required by Jackson for deserialization
    public CustomerEvent() {
        this.customerId = null;
        this.eventType = null;
        this.sessionCount = 0;
        this.daysSinceLastPurchase = 0;
        this.totalSpend = 0.0;
        this.timestamp = null;
    }

    public CustomerEvent(
            String customerId,
            String eventType,
            int sessionCount,
            int daysSinceLastPurchase,
            double totalSpend,
            String timestamp) {
        this.customerId = customerId;
        this.eventType = eventType;
        this.sessionCount = sessionCount;
        this.daysSinceLastPurchase = daysSinceLastPurchase;
        this.totalSpend = totalSpend;
        this.timestamp = timestamp;
    }

    public String getCustomerId() { return customerId; }
    public String getEventType() { return eventType; }
    public int getSessionCount() { return sessionCount; }
    public int getDaysSinceLastPurchase() { return daysSinceLastPurchase; }
    public double getTotalSpend() { return totalSpend; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "CustomerEvent{" +
                "customerId='" + customerId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", sessionCount=" + sessionCount +
                ", daysSinceLastPurchase=" + daysSinceLastPurchase +
                ", totalSpend=" + totalSpend +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
