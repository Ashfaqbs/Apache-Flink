package com.example.ai.retail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Output of SalesVelocityAgent (Agent 1). Contains the AI-assessed sales velocity
 * and estimated time to stockout for a single SKU. Keyed by skuId in the tumbling
 * window for aggregation into a SkuWindowSummary.
 */
public class SkuVelocityResult {

    @JsonProperty("skuId")
    private final String skuId;

    @JsonProperty("productName")
    private final String productName;

    // Velocity classification: HIGH, MEDIUM, or LOW
    @JsonProperty("salesVelocity")
    private final String salesVelocity;

    // Estimated days until stock hits zero based on current sell-through rate
    @JsonProperty("estimatedDaysToStockout")
    private final int estimatedDaysToStockout;

    // Human-readable stock concerns identified by the LLM
    @JsonProperty("stockConcerns")
    private final List<String> stockConcerns;

    // No-arg constructor required by Jackson for deserialization
    public SkuVelocityResult() {
        this.skuId = null;
        this.productName = null;
        this.salesVelocity = null;
        this.estimatedDaysToStockout = 0;
        this.stockConcerns = null;
    }

    public SkuVelocityResult(
            String skuId,
            String productName,
            String salesVelocity,
            int estimatedDaysToStockout,
            List<String> stockConcerns) {
        this.skuId = skuId;
        this.productName = productName;
        this.salesVelocity = salesVelocity;
        this.estimatedDaysToStockout = estimatedDaysToStockout;
        this.stockConcerns = stockConcerns;
    }

    public String getSkuId() { return skuId; }
    public String getProductName() { return productName; }
    public String getSalesVelocity() { return salesVelocity; }
    public int getEstimatedDaysToStockout() { return estimatedDaysToStockout; }
    public List<String> getStockConcerns() { return stockConcerns; }

    @Override
    public String toString() {
        return "SkuVelocityResult{" +
                "skuId='" + skuId + '\'' +
                ", productName='" + productName + '\'' +
                ", salesVelocity='" + salesVelocity + '\'' +
                ", estimatedDaysToStockout=" + estimatedDaysToStockout +
                ", stockConcerns=" + stockConcerns +
                '}';
    }
}
