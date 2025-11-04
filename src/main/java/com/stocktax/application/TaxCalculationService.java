package com.stocktax.application;

import com.stocktax.domain.TaxCalculator;
import com.stocktax.domain.model.Operation;
import com.stocktax.domain.model.TaxCalculation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TaxCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(TaxCalculationService.class);

    private final TaxCalculator taxCalculator;

    public TaxCalculationService(TaxCalculator taxCalculator) {
        this.taxCalculator = taxCalculator;
    }

    public List<TaxCalculation> calculateTaxes(List<Operation> operations) {
        if (operations == null || operations.isEmpty()) {
            logger.warn("Empty operations list provided");
            return List.of();
        }
        try {
            return taxCalculator.calculateTaxes(operations);
        } catch (Exception e) {
            logger.error("Error calculating taxes", e);
            throw new TaxCalculationException("Failed to calculate taxes", e);
        }
    }

    public static class TaxCalculationException extends RuntimeException {
        private static final long serialVersionUID = -7084735575365355314L;

		public TaxCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
