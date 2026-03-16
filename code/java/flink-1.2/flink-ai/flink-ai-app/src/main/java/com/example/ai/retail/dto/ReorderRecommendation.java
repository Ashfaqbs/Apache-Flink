package com.example.ai.retail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Final output of the inventory reorder pipeline, produced by ReorderAgent (Agent 2).
 * Contains the urgency level, suggested order quantity, and reasoning for the restock
 * recommendation. Written to the reorder-recommendations Kafka topic.
 */
public class ReorderRecommendation {

    @JsonProperty("skuId")
    private final String skuId;

    @JsonProperty("productName")
    private final String productName;

    @JsonProperty("warehouseId")
    private final String warehouseId;

    // Urgency: CRITICAL, HIGH, MEDIUM, or LOW
    @JsonProperty("urgency")
    private final String urgency;

    // Suggested number of units to order from the supplier
    @JsonProperty("suggestedOrderQuantity")
    private final int suggestedOrderQuantity;

    // Human-readable recommendations from the LLM supply chain persona
    @JsonProperty("recommendations")
    private final List<String> recommendations;

    // No-arg constructor required by Jackson for deserialization
    public ReorderRecommendation() {
        this.skuId = null;
        this.productName = null;
        this.warehouseId = null;
        this.urgency = null;
        this.suggestedOrderQuantity = 0;
        this.recommendations = null;
    }

    public ReorderRecommendation(
            String skuId,
            String productName,
            String warehouseId,
            String urgency,
            int suggestedOrderQuantity,
            List<String> recommendations) {
        this.skuId = skuId;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.urgency = urgency;
        this.suggestedOrderQuantity = suggestedOrderQuantity;
        this.recommendations = recommendations;
    }

    public String getSkuId() { return skuId; }
    public String getProductName() { return productName; }
    public String getWarehouseId() { return warehouseId; }
    public String getUrgency() { return urgency; }
    public int getSuggestedOrderQuantity() { return suggestedOrderQuantity; }
    public List<String> getRecommendations() { return recommendations; }

    @Override
    public String toString() {
        return "ReorderRecommendation{" +
                "skuId='" + skuId + '\'' +
                ", productName='" + productName + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", urgency='" + urgency + '\'' +
                ", suggestedOrderQuantity=" + suggestedOrderQuantity +
                ", recommendations=" + recommendations +
                '}';
    }
}
