package com.stocktax.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.stocktax.domain.model.Operation;
import com.stocktax.domain.model.StockPosition;
import com.stocktax.domain.model.TaxCalculation;


public class TaxCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");
    private static final BigDecimal TAX_THRESHOLD = new BigDecimal("20000.00");

    public List<TaxCalculation> calculateTaxes(List<Operation> operations) {
        List<TaxCalculation> results = new ArrayList<>();
        StockPosition position = new StockPosition();

        for (int i = 0; i < operations.size(); i++) {
            Operation operation = operations.get(i);
            TaxCalculation tax = calculateTaxForOperation(operation, position);
            results.add(tax);
        }
        return results;
    }

    private TaxCalculation calculateTaxForOperation(Operation operation, StockPosition position) {
        if (operation.isBuy()) {
            return handleBuyOperation(operation, position);
        } else {
            return handleSellOperation(operation, position);
        }
    }

    private TaxCalculation handleBuyOperation(Operation operation, StockPosition position) {
        position.addStocks(operation.getQuantity(), operation.getUnitCost());
        return new TaxCalculation(BigDecimal.ZERO);
    }

    private TaxCalculation handleSellOperation(Operation operation, StockPosition position) {
        BigDecimal profitOrLoss = position.sellStocks(operation.getQuantity(), operation.getUnitCost());

        BigDecimal operationAmount = operation.getTotalAmount();
        if (operationAmount.compareTo(TAX_THRESHOLD) <= 0) {
            // We still need to handle losses even if no tax
            if (profitOrLoss.compareTo(BigDecimal.ZERO) < 0) {
                position.addLoss(profitOrLoss.abs());
            }
            return new TaxCalculation(BigDecimal.ZERO);
        }

        if (profitOrLoss.compareTo(BigDecimal.ZERO) <= 0) {
            if (profitOrLoss.compareTo(BigDecimal.ZERO) < 0) {
                position.addLoss(profitOrLoss.abs());
            }
            return new TaxCalculation(BigDecimal.ZERO);
        }

        // we do have a profit -> calculate tax
        BigDecimal taxableProfit = calculateTaxableProfit(profitOrLoss, position);
        BigDecimal tax = taxableProfit.multiply(TAX_RATE);

        return new TaxCalculation(tax);
    }

    /**
     * Calculates taxable profit considering accumulated losses
     */
    private BigDecimal calculateTaxableProfit(BigDecimal currentProfit, StockPosition position) {
        BigDecimal accumulatedLosses = position.getAccumulatedLosses();

        if (accumulatedLosses.compareTo(BigDecimal.ZERO) == 0) {
            return currentProfit;
        }

        if (currentProfit.compareTo(accumulatedLosses) >= 0) {
            // current profit covers all accumulated losses
            BigDecimal taxableProfit = currentProfit.subtract(accumulatedLosses);
            position.reduceLosses(accumulatedLosses);
            return taxableProfit;
        } else {
            // Current profit doesn't cover all losses
            position.reduceLosses(currentProfit);
            return BigDecimal.ZERO;
        }
    }
}
