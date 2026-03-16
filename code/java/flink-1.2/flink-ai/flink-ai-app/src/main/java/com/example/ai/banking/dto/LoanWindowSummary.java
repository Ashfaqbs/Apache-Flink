package com.example.ai.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Aggregated summary produced by the tumbling window in LoanRiskPipelineJob.
 * Combines all LoanRiskSignals for a single applicant within a 5-minute window
 * and serialized to JSON before being passed to LoanRiskReportAgent (Agent 2).
 */
public class LoanWindowSummary {

    @JsonProperty("applicantId")
    private final String applicantId;

    // Number of loan applications submitted by this applicant in the window
    @JsonProperty("applicationCount")
    private final int applicationCount;

    // Mean risk score across all applications in the window
    @JsonProperty("avgRiskScore")
    private final double avgRiskScore;

    // Union of all risk factors identified across all applications
    @JsonProperty("allRiskFactors")
    private final List<String> allRiskFactors;

    // Per-application recommendations collected for final decision context
    @JsonProperty("recommendations")
    private final List<String> recommendations;

    // No-arg constructor required by Jackson for deserialization
    public LoanWindowSummary() {
        this.applicantId = null;
        this.applicationCount = 0;
        this.avgRiskScore = 0.0;
        this.allRiskFactors = null;
        this.recommendations = null;
    }

    public LoanWindowSummary(
            String applicantId,
            int applicationCount,
            double avgRiskScore,
            List<String> allRiskFactors,
            List<String> recommendations) {
        this.applicantId = applicantId;
        this.applicationCount = applicationCount;
        this.avgRiskScore = avgRiskScore;
        this.allRiskFactors = allRiskFactors;
        this.recommendations = recommendations;
    }

    public String getApplicantId() { return applicantId; }
    public int getApplicationCount() { return applicationCount; }
    public double getAvgRiskScore() { return avgRiskScore; }
    public List<String> getAllRiskFactors() { return allRiskFactors; }
    public List<String> getRecommendations() { return recommendations; }

    @Override
    public String toString() {
        return "LoanWindowSummary{" +
                "applicantId='" + applicantId + '\'' +
                ", applicationCount=" + applicationCount +
                ", avgRiskScore=" + avgRiskScore +
                ", allRiskFactors=" + allRiskFactors +
                ", recommendations=" + recommendations +
                '}';
    }
}
