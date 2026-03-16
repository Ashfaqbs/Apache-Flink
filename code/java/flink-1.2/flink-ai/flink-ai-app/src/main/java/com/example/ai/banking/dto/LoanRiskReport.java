package com.example.ai.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Final output of the loan risk pipeline, produced by LoanRiskReportAgent (Agent 2).
 * Contains the definitive lending decision for an applicant based on aggregated
 * risk signals and is written to the loan-risk-reports Kafka topic.
 */
public class LoanRiskReport {

    @JsonProperty("applicantId")
    private final String applicantId;

    // Total applications analyzed in the window for this applicant
    @JsonProperty("applicationCount")
    private final int applicationCount;

    // Final underwriting decision: APPROVE, REVIEW, or DECLINE
    @JsonProperty("finalDecision")
    private final String finalDecision;

    // Detailed reasoning points supporting the final decision
    @JsonProperty("justification")
    private final List<String> justification;

    // Averaged risk score from the window summary — preserved for audit trail
    @JsonProperty("avgRiskScore")
    private final double avgRiskScore;

    // No-arg constructor required by Jackson for deserialization
    public LoanRiskReport() {
        this.applicantId = null;
        this.applicationCount = 0;
        this.finalDecision = null;
        this.justification = null;
        this.avgRiskScore = 0.0;
    }

    public LoanRiskReport(
            String applicantId,
            int applicationCount,
            String finalDecision,
            List<String> justification,
            double avgRiskScore) {
        this.applicantId = applicantId;
        this.applicationCount = applicationCount;
        this.finalDecision = finalDecision;
        this.justification = justification;
        this.avgRiskScore = avgRiskScore;
    }

    public String getApplicantId() { return applicantId; }
    public int getApplicationCount() { return applicationCount; }
    public String getFinalDecision() { return finalDecision; }
    public List<String> getJustification() { return justification; }
    public double getAvgRiskScore() { return avgRiskScore; }

    @Override
    public String toString() {
        return "LoanRiskReport{" +
                "applicantId='" + applicantId + '\'' +
                ", applicationCount=" + applicationCount +
                ", finalDecision='" + finalDecision + '\'' +
                ", justification=" + justification +
                ", avgRiskScore=" + avgRiskScore +
                '}';
    }
}
