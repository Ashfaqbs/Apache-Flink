package com.example.ai.reviewpipeline.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Aggregated window output passed from the tumbling window to Agent 2.
 * Groups all review analysis results for a product within a time window:
 * a score histogram (% of 1-star, 2-star ... 5-star) and all collected
 * dissatisfaction reasons.
 */
@JsonSerialize
@JsonDeserialize
public class ReviewWindowSummary {

    private final String id;
    private final List<String> scoreHistogram;
    private final List<String> unsatisfiedReasons;

    @JsonCreator
    public ReviewWindowSummary(
            @JsonProperty("id") String id,
            @JsonProperty("scoreHistogram") List<String> scoreHistogram,
            @JsonProperty("unsatisfiedReasons") List<String> unsatisfiedReasons) {
        this.id = id;
        this.scoreHistogram = scoreHistogram;
        this.unsatisfiedReasons = unsatisfiedReasons;
    }

    public String getId() { return id; }
    public List<String> getScoreHistogram() { return scoreHistogram; }
    public List<String> getUnsatisfiedReasons() { return unsatisfiedReasons; }

    @Override
    public String toString() {
        return String.format(
                "ReviewWindowSummary{id='%s', scoreHistogram=%s, unsatisfiedReasons=%s}",
                id, scoreHistogram, unsatisfiedReasons);
    }
}
