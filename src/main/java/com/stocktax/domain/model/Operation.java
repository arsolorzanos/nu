package com.stocktax.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a stock market operation (buy or sell)
 */
public class Operation {
    
    public enum Type {
        BUY, SELL
    }
    
    private final Type operation;
    private final BigDecimal unitCost;
    private final int quantity;
    
    public Operation(String operation, BigDecimal unitCost, int quantity) {
        this.operation = Type.valueOf(operation.toUpperCase());
        this.unitCost = unitCost;
        this.quantity = quantity;
    }
    
    public Type getOperation() {
        return operation;
    }
    
    public BigDecimal getUnitCost() {
        return unitCost;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public BigDecimal getTotalAmount() {
        return unitCost.multiply(BigDecimal.valueOf(quantity));
    }
    
    public boolean isBuy() {
        return operation == Type.BUY;
    }
    
    public boolean isSell() {
        return operation == Type.SELL;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation1 = (Operation) o;
        return quantity == operation1.quantity &&
                operation == operation1.operation &&
                Objects.equals(unitCost, operation1.unitCost);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(operation, unitCost, quantity);
    }
    
    @Override
    public String toString() {
        return "Operation{" +
                "operation=" + operation +
                ", unitCost=" + unitCost +
                ", quantity=" + quantity +
                '}';
    }
}
