package com.stocktax.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the tax calculation result for an operation
 */
public class TaxCalculation {
    
    private final BigDecimal tax;
    
    public TaxCalculation(BigDecimal tax) {
        this.tax = tax;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxCalculation that = (TaxCalculation) o;
        return Objects.equals(tax, that.tax);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tax);
    }
    
    @Override
    public String toString() {
        return "TaxCalculation{" +
                "tax=" + tax +
                '}';
    }
}
