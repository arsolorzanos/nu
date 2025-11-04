package com.stocktax.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stocktax.domain.model.Operation;
import com.stocktax.domain.model.TaxCalculation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaxCalculatorTest {
    
    private TaxCalculator taxCalculator;
    
    @BeforeEach
    void setUp() {
        taxCalculator = new TaxCalculator();
    }
    
    @Test
    void testBuyOperationShouldNotPayTax() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("10.00"), 1000)
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(1, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax());
    }
    
    @Test
    void testSellOperationBelowThresholdShouldNotPayTax() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("10.00"), 1000),
            new Operation("sell", new BigDecimal("15.00"), 1000) // Total: 15,000 < 20,000
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(2, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax()); // Buy
        assertEquals(BigDecimal.ZERO, results.get(1).getTax()); // Sell below threshold
    }
    
    @Test
    void testSellOperationAboveThresholdWithProfitShouldPayTax() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("10.00"), 2000),
            new Operation("sell", new BigDecimal("20.00"), 1001) // Total: 20,020, Profit: 10,010
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(2, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax()); // Buy
        assertEquals(new BigDecimal("2002.0000"), results.get(1).getTax()); // 20% of 10,010
    }
    
    @Test
    void testSellOperationWithLossShouldNotPayTax() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("20.00"), 2000),
            new Operation("sell", new BigDecimal("10.00"), 1000) // Total: 10,000, Loss: 10,000
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(2, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax()); // Buy
        assertEquals(BigDecimal.ZERO, results.get(1).getTax()); // Loss
    }
    
    @Test
    void testWeightedAverageCalculation() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("10.00"), 1000),  // 10,000
            new Operation("buy", new BigDecimal("20.00"), 1000),  // 20,000 - weighted avg: 15.00
            new Operation("sell", new BigDecimal("25.00"), 1000)  // 25,000, profit: 10,000
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(3, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax()); // Buy
        assertEquals(BigDecimal.ZERO, results.get(1).getTax()); // Buy
        assertEquals(new BigDecimal("2000.0000"), results.get(2).getTax()); // 20% of 10,000
    }
    
    @Test
    void testAccumulatedLossesDeduction() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("20.00"), 2000),
            new Operation("sell", new BigDecimal("10.00"), 1000), // Loss: 10,000
            new Operation("sell", new BigDecimal("30.00"), 1000)  // Profit: 10,000, but covers loss
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(3, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax()); // Buy
        assertEquals(BigDecimal.ZERO, results.get(1).getTax()); // Loss
        assertEquals(new BigDecimal("0.0000"), results.get(2).getTax()); // Profit covers loss
    }
    
    @Test
    void testComplexScenario() {
        List<Operation> operations = Arrays.asList(
            new Operation("buy", new BigDecimal("10.00"), 10000),
            new Operation("sell", new BigDecimal("20.00"), 5000)
        );
        
        List<TaxCalculation> results = taxCalculator.calculateTaxes(operations);
        
        assertEquals(2, results.size());
        assertEquals(BigDecimal.ZERO, results.get(0).getTax()); // Buy
        assertEquals(new BigDecimal("10000.0000"), results.get(1).getTax()); // 20% of 50,000 profit
    }
}
