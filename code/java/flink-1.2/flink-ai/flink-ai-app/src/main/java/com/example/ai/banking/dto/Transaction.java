package com.example.ai.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents a single bank card transaction arriving from the Kafka source topic.
 * Used as input to the FraudDetectionAgent for real-time fraud analysis.
 */
public class Transaction {

    @JsonProperty("transactionId")
    private final String transactionId;

    @JsonProperty("accountId")
    private final String accountId;

    // Transaction amount in account's base currency
    @JsonProperty("amount")
    private final double amount;

    // e.g. "grocery", "electronics", "foreign_atm", "online_gambling"
    @JsonProperty("merchantCategory")
    private final String merchantCategory;

    // ISO country code or city name — used to detect geographic anomalies
    @JsonProperty("merchantLocation")
    private final String merchantLocation;

    // false = card not present (CNP) transaction, a common fraud signal
    @JsonProperty("cardPresent")
    private final boolean cardPresent;

    @JsonProperty("timestamp")
    private final String timestamp;

    // No-arg constructor required by Jackson for deserialization
    public Transaction() {
        this.transactionId = null;
        this.accountId = null;
        this.amount = 0.0;
        this.merchantCategory = null;
        this.merchantLocation = null;
        this.cardPresent = false;
        this.timestamp = null;
    }

    public Transaction(
            String transactionId,
            String accountId,
            double amount,
            String merchantCategory,
            String merchantLocation,
            boolean cardPresent,
            String timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.merchantCategory = merchantCategory;
        this.merchantLocation = merchantLocation;
        this.cardPresent = cardPresent;
        this.timestamp = timestamp;
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public double getAmount() { return amount; }
    public String getMerchantCategory() { return merchantCategory; }
    public String getMerchantLocation() { return merchantLocation; }
    public boolean isCardPresent() { return cardPresent; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", merchantCategory='" + merchantCategory + '\'' +
                ", merchantLocation='" + merchantLocation + '\'' +
                ", cardPresent=" + cardPresent +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
