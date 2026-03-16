package com.example.ai.reviewpipeline.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/** A single raw product review read from the input file. */
@JsonSerialize
@JsonDeserialize
public class ProductReview {

    private final String id;
    private final String review;

    @JsonCreator
    public ProductReview(
            @JsonProperty("id") String id,
            @JsonProperty("review") String review) {
        this.id = id;
        this.review = review;
    }

    public String getId() { return id; }
    public String getReview() { return review; }

    @Override
    public String toString() {
        return String.format("ProductReview{id='%s', review='%s'}", id, review);
    }
}
