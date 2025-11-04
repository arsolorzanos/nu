package com.stocktax.infrastructure.adapters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocktax.domain.model.Operation;
import com.stocktax.domain.ports.InputPort;
import com.stocktax.infrastructure.dto.OperationDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for reading JSON input from stdin
 */
public class JsonInputAdapter implements InputPort {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonInputAdapter.class);
    private final ObjectMapper objectMapper;
    
    public JsonInputAdapter() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Reads operations from stdin line by line
     */
    @Override
    public List<List<Operation>> readOperations() throws IOException {
        List<List<Operation>> allOperations = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            jsonBuffer.append(line);
            try {
                List<Operation> operations = parseOperationsLine(jsonBuffer.toString());
                allOperations.add(operations);
                jsonBuffer.setLength(0);
            } catch (Exception e) {
                logger.debug("JSON not complete yet, continuing to read lines");
            }
        }
        
        if (jsonBuffer.length() > 0) {
            try {
                List<Operation> operations = parseOperationsLine(jsonBuffer.toString());
                allOperations.add(operations);
                logger.debug("Successfully parsed {} operations from final buffer", operations.size());
            } catch (Exception e) {
                logger.error("Error parsing final JSON buffer: {}", jsonBuffer, e);
                throw new IOException("Failed to parse operations: " + e.getMessage(), e);
            }
        }

        return allOperations;
    }
    
    private List<Operation> parseOperationsLine(String line) throws IOException {
        try {
            List<OperationDto> dtos = objectMapper.readValue(line, new TypeReference<List<OperationDto>>() {});
            return dtos.stream()
                    .map(dto -> new Operation(dto.getOperation(), dto.getUnitCost(), dto.getQuantity()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to parse JSON line: {}", line, e);
            throw new IOException("Invalid JSON format: " + e.getMessage(), e);
        }
    }
}
