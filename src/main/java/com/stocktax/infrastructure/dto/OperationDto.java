package com.stocktax.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Data Transfer Object for JSON operations
 */
public class OperationDto {
    
    @JsonProperty("operation")
    private String operation;
    
    @JsonProperty("unit-cost")
    private BigDecimal unitCost;
    
    @JsonProperty("quantity")
    private int quantity;
    
    public OperationDto() {
    }
    
    public OperationDto(String operation, BigDecimal unitCost, int quantity) {
        this.operation = operation;
        this.unitCost = unitCost;
        this.quantity = quantity;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public BigDecimal getUnitCost() {
        return unitCost;
    }
    
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

