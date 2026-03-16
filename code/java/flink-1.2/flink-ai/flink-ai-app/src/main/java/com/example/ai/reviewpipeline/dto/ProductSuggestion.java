package com.example.ai.reviewpipeline.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Final output of the pipeline — produced by Agent 2 (ProductSuggestionAgent).
 * Contains actionable improvement suggestions for a product derived from the
 * aggregated review window. This is what gets sinked to Kafka.
 */
@JsonSerialize
@JsonDeserialize
public class ProductSuggestion {

    private final String productId;
    private final List<String> scoreHistogram;
    private final List<String> suggestions;

    @JsonCreator
    public ProductSuggestion(
            @JsonProperty("productId") String productId,
            @JsonProperty("scoreHistogram") List<String> scoreHistogram,
            @JsonProperty("suggestions") List<String> suggestions) {
        this.productId = productId;
        this.scoreHistogram = scoreHistogram;
        this.suggestions = suggestions;
    }

    public String getProductId() { return productId; }
    public List<String> getScoreHistogram() { return scoreHistogram; }
    public List<String> getSuggestions() { return suggestions; }

    @Override
    public String toString() {
        return String.format(
                "ProductSuggestion{productId='%s', scoreHistogram=%s, suggestions=%s}",
                productId, scoreHistogram, suggestions);
    }
}
