package com.stocktax.application;

import com.stocktax.domain.TaxCalculator;
import com.stocktax.domain.model.Operation;
import com.stocktax.domain.model.TaxCalculation;
import com.stocktax.infrastructure.adapters.MockInputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaxCalculationServiceIntegrationTest {
    
    private TaxCalculationService taxCalculationService;
    
    @BeforeEach
    void setUp() {
        TaxCalculator taxCalculator = new TaxCalculator();
        taxCalculationService = new TaxCalculationService(taxCalculator);
    }
    
    @Test
    void testCalculateTaxesWithMockInputAdapter() {
        Operation buy = new Operation("buy", new BigDecimal("10.00"), 10000);
        Operation sell = new Operation("sell", new BigDecimal("20.00"), 5000);
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy, sell)
        );
        
        MockInputAdapter mockAdapter = new MockInputAdapter(operations);
        
        try {
            List<List<Operation>> inputOperations = mockAdapter.readOperations();
            assertEquals(1, inputOperations.size());
            
            List<TaxCalculation> results = taxCalculationService.calculateTaxes(inputOperations.get(0));
            
            assertEquals(2, results.size());
            assertEquals(BigDecimal.ZERO, results.get(0).getTax());
            assertEquals(new BigDecimal("10000.0000"), results.get(1).getTax());
        } catch (Exception e) {
            fail("Should not throw exception", e);
        }
    }
    
    @Test
    void testCalculateTaxesWithMultipleOperationSets() {
        Operation buy1 = new Operation("buy", new BigDecimal("10.00"), 1000);
        Operation sell1 = new Operation("sell", new BigDecimal("20.00"), 500);
        
        Operation buy2 = new Operation("buy", new BigDecimal("15.00"), 2000);
        Operation sell2 = new Operation("sell", new BigDecimal("25.00"), 1000);
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy1, sell1),
            Arrays.asList(buy2, sell2)
        );
        
        MockInputAdapter mockAdapter = new MockInputAdapter(operations);
        
        try {
            List<List<Operation>> inputOperations = mockAdapter.readOperations();
            assertEquals(2, inputOperations.size());
            
            // Process first set
            List<TaxCalculation> results1 = taxCalculationService.calculateTaxes(inputOperations.get(0));
            assertEquals(2, results1.size());
            assertEquals(0, results1.get(0).getTax().compareTo(BigDecimal.ZERO));
            assertEquals(0, results1.get(1).getTax().compareTo(BigDecimal.ZERO)); // 500 shares × $20 = $10,000 < $20,000 threshold, no tax
            
            // Process second set
            List<TaxCalculation> results2 = taxCalculationService.calculateTaxes(inputOperations.get(1));
            assertEquals(2, results2.size());
            assertEquals(BigDecimal.ZERO, results2.get(0).getTax());
            assertEquals(new BigDecimal("2000.0000"), results2.get(1).getTax()); // 20% of 10,000 profit
        } catch (Exception e) {
            fail("Should not throw exception", e);
        }
    }
    
    @Test
    void testCalculateTaxesWithEmptyList() {
        List<List<Operation>> operations = Arrays.asList();
        
        MockInputAdapter mockAdapter = new MockInputAdapter(operations);
        
        try {
            List<List<Operation>> inputOperations = mockAdapter.readOperations();
            assertTrue(inputOperations.isEmpty());
            
            // Should handle empty list gracefully
            List<TaxCalculation> results = taxCalculationService.calculateTaxes(Arrays.asList());
            assertTrue(results.isEmpty());
        } catch (Exception e) {
            fail("Should not throw exception", e);
        }
    }
}

