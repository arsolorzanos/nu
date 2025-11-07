package com.stocktax.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the current stock position with weighted average price and accumulated losses
 */
public class StockPosition {
    
    private int totalQuantity;
    private BigDecimal weightedAveragePrice;
    private BigDecimal accumulatedLosses;
    
    public StockPosition() {
        this.totalQuantity = 0;
        this.weightedAveragePrice = BigDecimal.ZERO;
        this.accumulatedLosses = BigDecimal.ZERO;
    }
    
    public int getTotalQuantity() {
        return totalQuantity;
    }
    
    public BigDecimal getWeightedAveragePrice() {
        return weightedAveragePrice;
    }
    
    public BigDecimal getAccumulatedLosses() {
        return accumulatedLosses;
    }
    
    /**
     * Updates when buying stocks
     */
    public void addStocks(int quantity, BigDecimal unitPrice) {
        if (totalQuantity == 0) {
            this.weightedAveragePrice = unitPrice;
        } else {
            BigDecimal currentValue = weightedAveragePrice.multiply(BigDecimal.valueOf(totalQuantity));
            BigDecimal newValue = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal totalValue = currentValue.add(newValue);
            int totalQty = totalQuantity + quantity;
            
            this.weightedAveragePrice = totalValue.divide(BigDecimal.valueOf(totalQty), 2, java.math.RoundingMode.HALF_UP);
        }
        
        this.totalQuantity += quantity;
    }
    
    /**
     * Updates when selling stocks and returns the profit/loss
     */
    public BigDecimal sellStocks(int quantity, BigDecimal unitPrice) {
        if (quantity > totalQuantity) {
            throw new IllegalArgumentException("Cannot sell more stocks than available");
        }
        
        BigDecimal totalCost = weightedAveragePrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalRevenue = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal profitOrLoss = totalRevenue.subtract(totalCost);
        
        this.totalQuantity -= quantity;
        
        return profitOrLoss;
    }
    
    public void processLoss(BigDecimal profitOrLoss) {
        if (profitOrLoss.compareTo(BigDecimal.ZERO) < 0) {
            this.accumulatedLosses = this.accumulatedLosses.add(profitOrLoss.abs());
        }
    }
    
    public BigDecimal calculateTaxableProfit(BigDecimal profit) {
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Profit must be positive");
        }
        
        if (accumulatedLosses.compareTo(BigDecimal.ZERO) == 0) {
            return profit;
        }
        
        if (profit.compareTo(accumulatedLosses) >= 0) {
            // Current profit covers all accumulated losses
            BigDecimal taxableProfit = profit.subtract(accumulatedLosses);
            this.accumulatedLosses = BigDecimal.ZERO;
            return taxableProfit;
        } else {
            // Current profit doesn't cover all losses
            this.accumulatedLosses = this.accumulatedLosses.subtract(profit);
            return BigDecimal.ZERO;
        }
    }
    
    public boolean hasStocks() {
        return totalQuantity > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockPosition that = (StockPosition) o;
        return totalQuantity == that.totalQuantity &&
                Objects.equals(weightedAveragePrice, that.weightedAveragePrice) &&
                Objects.equals(accumulatedLosses, that.accumulatedLosses);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalQuantity, weightedAveragePrice, accumulatedLosses);
    }
    
    @Override
    public String toString() {
        return "StockPosition{" +
                "totalQuantity=" + totalQuantity +
                ", weightedAveragePrice=" + weightedAveragePrice +
                ", accumulatedLosses=" + accumulatedLosses +
                '}';
    }
}
