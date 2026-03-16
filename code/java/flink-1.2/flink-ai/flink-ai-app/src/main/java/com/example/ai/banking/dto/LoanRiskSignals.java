package com.example.ai.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Output of LoanRiskExtractionAgent (Agent 1). Contains the AI-extracted risk
 * score and factors for a single loan application. Keyed by applicantId in the
 * tumbling window for aggregation into a LoanWindowSummary.
 */
public class LoanRiskSignals {

    @JsonProperty("applicationId")
    private final String applicationId;

    @JsonProperty("applicantId")
    private final String applicantId;

    // Risk score from 1 (lowest) to 10 (highest) — assigned by the LLM
    @JsonProperty("riskScore")
    private final int riskScore;

    // Human-readable risk factors identified by the LLM
    @JsonProperty("riskFactors")
    private final List<String> riskFactors;

    // Preliminary recommendation: APPROVE, REVIEW, or DECLINE
    @JsonProperty("approvalRecommendation")
    private final String approvalRecommendation;

    // No-arg constructor required by Jackson for deserialization
    public LoanRiskSignals() {
        this.applicationId = null;
        this.applicantId = null;
        this.riskScore = 0;
        this.riskFactors = null;
        this.approvalRecommendation = null;
    }

    public LoanRiskSignals(
            String applicationId,
            String applicantId,
            int riskScore,
            List<String> riskFactors,
            String approvalRecommendation) {
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.riskScore = riskScore;
        this.riskFactors = riskFactors;
        this.approvalRecommendation = approvalRecommendation;
    }

    public String getApplicationId() { return applicationId; }
    public String getApplicantId() { return applicantId; }
    public int getRiskScore() { return riskScore; }
    public List<String> getRiskFactors() { return riskFactors; }
    public String getApprovalRecommendation() { return approvalRecommendation; }

    @Override
    public String toString() {
        return "LoanRiskSignals{" +
                "applicationId='" + applicationId + '\'' +
                ", applicantId='" + applicantId + '\'' +
                ", riskScore=" + riskScore +
                ", riskFactors=" + riskFactors +
                ", approvalRecommendation='" + approvalRecommendation + '\'' +
                '}';
    }
}
