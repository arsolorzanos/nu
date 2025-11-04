package com.stocktax.infrastructure.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocktax.domain.model.TaxCalculation;
import com.stocktax.domain.ports.OutputPort;
import com.stocktax.infrastructure.dto.TaxCalculationDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for writing JSON output to stdout
 */
public class JsonOutputAdapter implements OutputPort {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonOutputAdapter.class);
    private final ObjectMapper objectMapper;
    
    public JsonOutputAdapter() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Writes tax calculations as JSON to stdout
     */
    @Override
    public void writeTaxCalculations(List<TaxCalculation> taxCalculations) throws IOException {
        try {
            // Convert domain objects to DTOs for JSON serialization
            List<TaxCalculationDto> dtos = taxCalculations.stream()
                    .map(tc -> new TaxCalculationDto(tc.getTax()))
                    .collect(Collectors.toList());
            
            String json = objectMapper.writeValueAsString(dtos);
            logger.info(json);
        } catch (Exception e) {
            logger.error("Error writing tax calculations", e);
            throw new IOException("Failed to write output: " + e.getMessage(), e);
        }
    }
}
