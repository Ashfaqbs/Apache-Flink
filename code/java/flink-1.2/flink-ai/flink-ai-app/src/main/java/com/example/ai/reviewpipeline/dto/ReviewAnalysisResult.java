package com.example.ai.reviewpipeline.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Output of Agent 1 (ReviewAnalysisAgent).
 * One result per review: a satisfaction score (1-5) and reasons for dissatisfaction.
 * This is what gets windowed and aggregated before Agent 2 sees it.
 */
@JsonSerialize
@JsonDeserialize
public class ReviewAnalysisResult {

    private final String id;
    private final int score;
    private final List<String> reasons;

    @JsonCreator
    public ReviewAnalysisResult(
            @JsonProperty("id") String id,
            @JsonProperty("score") int score,
            @JsonProperty("reasons") List<String> reasons) {
        this.id = id;
        this.score = score;
        this.reasons = reasons;
    }

    // Required for Flink deserialization in window functions
    public ReviewAnalysisResult() {
        this.id = null;
        this.score = 0;
        this.reasons = List.of();
    }

    public String getId() { return id; }
    public int getScore() { return score; }
    public List<String> getReasons() { return reasons; }

    @Override
    public String toString() {
        return String.format("ReviewAnalysisResult{id='%s', score=%d, reasons=%s}", id, score, reasons);
    }
}
