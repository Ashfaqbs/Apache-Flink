package com.example.ai.retail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a sale transaction event for a specific SKU at a warehouse.
 * Input to the SalesVelocityAgent for per-SKU stock depletion analysis.
 */
public class SaleEvent {

    @JsonProperty("skuId")
    private final String skuId;

    @JsonProperty("productName")
    private final String productName;

    // Number of units sold in this transaction
    @JsonProperty("quantitySold")
    private final int quantitySold;

    // Remaining stock after this sale — used to estimate days to stockout
    @JsonProperty("currentStock")
    private final int currentStock;

    // Warehouse identifier — reorder recommendations are per-warehouse
    @JsonProperty("warehouseId")
    private final String warehouseId;

    @JsonProperty("timestamp")
    private final String timestamp;

    // No-arg constructor required by Jackson for deserialization
    public SaleEvent() {
        this.skuId = null;
        this.productName = null;
        this.quantitySold = 0;
        this.currentStock = 0;
        this.warehouseId = null;
        this.timestamp = null;
    }

    public SaleEvent(
            String skuId,
            String productName,
            int quantitySold,
            int currentStock,
            String warehouseId,
            String timestamp) {
        this.skuId = skuId;
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.currentStock = currentStock;
        this.warehouseId = warehouseId;
        this.timestamp = timestamp;
    }

    public String getSkuId() { return skuId; }
    public String getProductName() { return productName; }
    public int getQuantitySold() { return quantitySold; }
    public int getCurrentStock() { return currentStock; }
    public String getWarehouseId() { return warehouseId; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "SaleEvent{" +
                "skuId='" + skuId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantitySold=" + quantitySold +
                ", currentStock=" + currentStock +
                ", warehouseId='" + warehouseId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
