package com.stocktax.application;

import com.stocktax.domain.TaxCalculator;
import com.stocktax.domain.model.Operation;
import com.stocktax.domain.model.TaxCalculation;
import com.stocktax.infrastructure.adapters.MockInputAdapter;
import com.stocktax.infrastructure.adapters.MockOutputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    
    private TaxCalculationService taxCalculationService;
    private MockInputAdapter mockInputAdapter;
    private MockOutputAdapter mockOutputAdapter;
    private Application application;
    
    @BeforeEach
    void setUp() {
        TaxCalculator taxCalculator = new TaxCalculator();
        taxCalculationService = new TaxCalculationService(taxCalculator);
        mockInputAdapter = new MockInputAdapter(new ArrayList<>());
        mockOutputAdapter = new MockOutputAdapter();
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
    }
    
    @Test
    void testRunWithSingleOperationList() throws IOException {
        Operation buy = new Operation("buy", new BigDecimal("10.00"), 10000);
        Operation sell = new Operation("sell", new BigDecimal("20.00"), 5000);
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy, sell)
        );
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(1, written.size());
        assertEquals(2, written.get(0).size());
        assertEquals(BigDecimal.ZERO, written.get(0).get(0).getTax()); // Buy operation
        assertEquals(new BigDecimal("10000.0000"), written.get(0).get(1).getTax()); // Sell operation
    }
    
    @Test
    void testRunWithMultipleOperationLists() throws IOException {
        Operation buy1 = new Operation("buy", new BigDecimal("10.00"), 10000);
        Operation sell1 = new Operation("sell", new BigDecimal("20.00"), 5000);
        
        Operation buy2 = new Operation("buy", new BigDecimal("20.00"), 10000);
        Operation sell2 = new Operation("sell", new BigDecimal("10.00"), 5000);
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy1, sell1),
            Arrays.asList(buy2, sell2)
        );
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(2, written.size());
        

        assertEquals(2, written.get(0).size());
        assertEquals(BigDecimal.ZERO, written.get(0).get(0).getTax());
        assertEquals(new BigDecimal("10000.0000"), written.get(0).get(1).getTax());
        
        assertEquals(2, written.get(1).size());
        assertEquals(BigDecimal.ZERO, written.get(1).get(0).getTax());
        assertEquals(BigDecimal.ZERO, written.get(1).get(1).getTax()); // Loss, no tax
    }
    
    @Test
    void testRunWithEmptyOperations() throws IOException {
        List<List<Operation>> operations = Arrays.asList();
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(0, written.size());
    }
    
    @Test
    void testRunWithBuyOperationOnly() throws IOException {
        Operation buy = new Operation("buy", new BigDecimal("10.00"), 1000);
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy)
        );
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(1, written.size());
        assertEquals(1, written.get(0).size());
        assertEquals(BigDecimal.ZERO, written.get(0).get(0).getTax());
    }
    
    @Test
    void testRunWithSellBelowThreshold() throws IOException {
        Operation buy = new Operation("buy", new BigDecimal("10.00"), 1000);
        Operation sell = new Operation("sell", new BigDecimal("15.00"), 1000); // Total: 15,000 < 20,000
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy, sell)
        );
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(1, written.size());
        assertEquals(2, written.get(0).size());
        assertEquals(BigDecimal.ZERO, written.get(0).get(0).getTax()); // Buy
        assertEquals(BigDecimal.ZERO, written.get(0).get(1).getTax()); // Sell below threshold
    }
    
    @Test
    void testRunWithWeightedAverage() throws IOException {
        Operation buy1 = new Operation("buy", new BigDecimal("10.00"), 1000);
        Operation buy2 = new Operation("buy", new BigDecimal("20.00"), 1000);
        Operation sell = new Operation("sell", new BigDecimal("25.00"), 1000); // Weighted avg: 15.00, Profit: 10,000
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy1, buy2, sell)
        );
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(1, written.size());
        assertEquals(3, written.get(0).size());
        assertEquals(BigDecimal.ZERO, written.get(0).get(0).getTax()); // Buy 1
        assertEquals(BigDecimal.ZERO, written.get(0).get(1).getTax()); // Buy 2
        assertEquals(new BigDecimal("2000.0000"), written.get(0).get(2).getTax()); // 20% of 10,000
    }
    
    @Test
    void testRunWithLossDeduction() throws IOException {
        Operation buy = new Operation("buy", new BigDecimal("20.00"), 2000);
        Operation sell1 = new Operation("sell", new BigDecimal("10.00"), 1000); // Loss: 10,000
        Operation sell2 = new Operation("sell", new BigDecimal("30.00"), 1000); // Profit: 10,000, covers loss
        
        List<List<Operation>> operations = Arrays.asList(
            Arrays.asList(buy, sell1, sell2)
        );
        
        mockInputAdapter = new MockInputAdapter(operations);
        application = new Application(taxCalculationService, mockInputAdapter, mockOutputAdapter);
        
        application.run();
        
        List<List<TaxCalculation>> written = mockOutputAdapter.getWrittenCalculations();
        assertEquals(1, written.size());
        assertEquals(3, written.get(0).size());
        assertEquals(0, written.get(0).get(0).getTax().compareTo(BigDecimal.ZERO)); // Buy
        assertEquals(0, written.get(0).get(1).getTax().compareTo(BigDecimal.ZERO)); // Loss
        assertEquals(0, written.get(0).get(2).getTax().compareTo(BigDecimal.ZERO)); // Profit covers loss
    }
}

