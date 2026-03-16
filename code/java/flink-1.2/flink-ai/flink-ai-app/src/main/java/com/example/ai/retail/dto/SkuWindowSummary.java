package com.example.ai.retail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Aggregated summary produced by the tumbling window in InventoryReorderJob.
 * Combines all SkuVelocityResults for a single SKU within a 5-minute window
 * and is serialized to JSON before being passed to ReorderAgent (Agent 2).
 */
public class SkuWindowSummary {

    @JsonProperty("skuId")
    private final String skuId;

    @JsonProperty("productName")
    private final String productName;

    @JsonProperty("warehouseId")
    private final String warehouseId;

    // Proxy for total units: element count in the window (since SkuVelocityResult
    // does not carry raw quantity — that information is consumed by Agent 1)
    @JsonProperty("totalUnitsSold")
    private final int totalUnitsSold;

    // Average estimated days to stockout across all velocity assessments in the window
    @JsonProperty("avgDaysToStockout")
    private final double avgDaysToStockout;

    // Minimum stock seen — 0 when stock data is unavailable from velocity results
    @JsonProperty("minStockSeen")
    private final int minStockSeen;

    // Union of all stock concerns identified by Agent 1 across the window
    @JsonProperty("allConcerns")
    private final List<String> allConcerns;

    // No-arg constructor required by Jackson for deserialization
    public SkuWindowSummary() {
        this.skuId = null;
        this.productName = null;
        this.warehouseId = null;
        this.totalUnitsSold = 0;
        this.avgDaysToStockout = 0.0;
        this.minStockSeen = 0;
        this.allConcerns = null;
    }

    public SkuWindowSummary(
            String skuId,
            String productName,
            String warehouseId,
            int totalUnitsSold,
            double avgDaysToStockout,
            int minStockSeen,
            List<String> allConcerns) {
        this.skuId = skuId;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.totalUnitsSold = totalUnitsSold;
        this.avgDaysToStockout = avgDaysToStockout;
        this.minStockSeen = minStockSeen;
        this.allConcerns = allConcerns;
    }

    public String getSkuId() { return skuId; }
    public String getProductName() { return productName; }
    public String getWarehouseId() { return warehouseId; }
    public int getTotalUnitsSold() { return totalUnitsSold; }
    public double getAvgDaysToStockout() { return avgDaysToStockout; }
    public int getMinStockSeen() { return minStockSeen; }
    public List<String> getAllConcerns() { return allConcerns; }

    @Override
    public String toString() {
        return "SkuWindowSummary{" +
                "skuId='" + skuId + '\'' +
                ", productName='" + productName + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", totalUnitsSold=" + totalUnitsSold +
                ", avgDaysToStockout=" + avgDaysToStockout +
                ", minStockSeen=" + minStockSeen +
                ", allConcerns=" + allConcerns +
                '}';
    }
}
