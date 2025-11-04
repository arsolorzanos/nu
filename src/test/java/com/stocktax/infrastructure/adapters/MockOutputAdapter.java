package com.stocktax.infrastructure.adapters;

import com.stocktax.domain.model.TaxCalculation;
import com.stocktax.domain.ports.OutputPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of OutputPort for testing purposes.
 */
public class MockOutputAdapter implements OutputPort {
    
    private final List<List<TaxCalculation>> writtenCalculations;
    
    public MockOutputAdapter() {
        this.writtenCalculations = new ArrayList<>();
    }
    
    @Override
    public void writeTaxCalculations(List<TaxCalculation> taxCalculations) throws IOException {
        writtenCalculations.add(new ArrayList<>(taxCalculations));
    }
    
    public List<List<TaxCalculation>> getWrittenCalculations() {
        return new ArrayList<>(writtenCalculations);
    }
    
    public void clear() {
        writtenCalculations.clear();
    }
}

