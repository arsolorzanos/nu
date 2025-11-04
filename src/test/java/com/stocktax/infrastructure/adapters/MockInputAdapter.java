package com.stocktax.infrastructure.adapters;

import com.stocktax.domain.model.Operation;
import com.stocktax.domain.ports.InputPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of InputPort for testing purposes.
 */
public class MockInputAdapter implements InputPort {
    
    private final List<List<Operation>> operations;
    
    public MockInputAdapter(List<List<Operation>> operations) {
        this.operations = new ArrayList<>(operations);
    }
    
    @Override
    public List<List<Operation>> readOperations() throws IOException {
        return new ArrayList<>(operations);
    }
}

