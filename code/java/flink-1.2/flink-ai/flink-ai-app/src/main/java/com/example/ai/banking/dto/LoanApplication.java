package com.example.ai.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a loan application submitted by a customer.
 * This is the input to the LoanRiskExtractionAgent (Agent 1) in the loan pipeline.
 */
public class LoanApplication {

    @JsonProperty("applicationId")
    private final String applicationId;

    @JsonProperty("applicantId")
    private final String applicantId;

    // Loan amount requested in base currency
    @JsonProperty("requestedAmount")
    private final double requestedAmount;

    // FICO-style credit score — key risk indicator
    @JsonProperty("creditScore")
    private final int creditScore;

    // Annual gross income — used for debt-to-income ratio calculation
    @JsonProperty("annualIncome")
    private final double annualIncome;

    // Years of continuous employment — short history increases risk
    @JsonProperty("employmentYears")
    private final int employmentYears;

    // Sum of all existing debt obligations (loans, credit cards, etc.)
    @JsonProperty("existingDebtAmount")
    private final double existingDebtAmount;

    @JsonProperty("timestamp")
    private final String timestamp;

    // No-arg constructor required by Jackson for deserialization
    public LoanApplication() {
        this.applicationId = null;
        this.applicantId = null;
        this.requestedAmount = 0.0;
        this.creditScore = 0;
        this.annualIncome = 0.0;
        this.employmentYears = 0;
        this.existingDebtAmount = 0.0;
        this.timestamp = null;
    }

    public LoanApplication(
            String applicationId,
            String applicantId,
            double requestedAmount,
            int creditScore,
            double annualIncome,
            int employmentYears,
            double existingDebtAmount,
            String timestamp) {
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.requestedAmount = requestedAmount;
        this.creditScore = creditScore;
        this.annualIncome = annualIncome;
        this.employmentYears = employmentYears;
        this.existingDebtAmount = existingDebtAmount;
        this.timestamp = timestamp;
    }

    public String getApplicationId() { return applicationId; }
    public String getApplicantId() { return applicantId; }
    public double getRequestedAmount() { return requestedAmount; }
    public int getCreditScore() { return creditScore; }
    public double getAnnualIncome() { return annualIncome; }
    public int getEmploymentYears() { return employmentYears; }
    public double getExistingDebtAmount() { return existingDebtAmount; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "LoanApplication{" +
                "applicationId='" + applicationId + '\'' +
                ", applicantId='" + applicantId + '\'' +
                ", requestedAmount=" + requestedAmount +
                ", creditScore=" + creditScore +
                ", annualIncome=" + annualIncome +
                ", employmentYears=" + employmentYears +
                ", existingDebtAmount=" + existingDebtAmount +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
