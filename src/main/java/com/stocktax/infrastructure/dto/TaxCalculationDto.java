package com.stocktax.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Data Transfer Object for JSON tax calculations
 */
public class TaxCalculationDto {
    
    @JsonProperty("tax")
    private BigDecimal tax;
    
    public TaxCalculationDto() {
    }
    
    public TaxCalculationDto(BigDecimal tax) {
        this.tax = tax;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
}

