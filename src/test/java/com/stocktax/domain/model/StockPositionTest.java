package com.stocktax.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockPositionTest {

  private StockPosition stockPosition;

  @BeforeEach
  void setUp() {
    stockPosition = new StockPosition();
  }

  @Test
  void testInitialState() {
    assertEquals(0, stockPosition.getTotalQuantity());
    assertEquals(BigDecimal.ZERO, stockPosition.getWeightedAveragePrice());
    assertEquals(BigDecimal.ZERO, stockPosition.getAccumulatedLosses());
    assertFalse(stockPosition.hasStocks());
  }

  @Test
  void testAddStocksFirstBuy() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));

    assertEquals(1000, stockPosition.getTotalQuantity());
    assertEquals(
      new BigDecimal("10.00"),
      stockPosition.getWeightedAveragePrice()
    );
    assertTrue(stockPosition.hasStocks());
  }

  @Test
  void testAddStocksMultipleBuysWeightedAverage() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));
    assertEquals(
      new BigDecimal("10.00"),
      stockPosition.getWeightedAveragePrice()
    );

    stockPosition.addStocks(1000, new BigDecimal("20.00"));
    assertEquals(2000, stockPosition.getTotalQuantity());
    assertEquals(
      new BigDecimal("15.00"),
      stockPosition.getWeightedAveragePrice()
    );
  }

  @Test
  void testAddStocksWeightedAverageWithDifferentQuantities() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));
    stockPosition.addStocks(2000, new BigDecimal("20.00"));

    assertEquals(3000, stockPosition.getTotalQuantity());
    assertEquals(
      new BigDecimal("16.67"),
      stockPosition.getWeightedAveragePrice()
    );
  }

  @Test
  void testSellStocksWithProfit() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));

    // Sell 500 @ $15.00
    // Cost: 500 * 10 = 5000
    // Revenue: 500 * 15 = 7500
    // Profit: 2500
    BigDecimal profitOrLoss = stockPosition.sellStocks(
      500,
      new BigDecimal("15.00")
    );

    assertEquals(new BigDecimal("2500.00"), profitOrLoss);
    assertEquals(500, stockPosition.getTotalQuantity());
    assertEquals(
      new BigDecimal("10.00"),
      stockPosition.getWeightedAveragePrice()
    );
  }

  @Test
  void testSellStocksWithLoss() {
    stockPosition.addStocks(1000, new BigDecimal("20.00"));

    // Sell 500 @ $10.00
    // Cost: 500 * 20 = 10000
    // Revenue: 500 * 10 = 5000
    // Loss: -5000
    BigDecimal profitOrLoss = stockPosition.sellStocks(
      500,
      new BigDecimal("10.00")
    );

    assertEquals(new BigDecimal("-5000.00"), profitOrLoss);
    assertEquals(500, stockPosition.getTotalQuantity());
  }

  @Test
  void testSellStocksAtBreakEven() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));

    BigDecimal profitOrLoss = stockPosition.sellStocks(
      500,
      new BigDecimal("10.00")
    );

    assertEquals(0, profitOrLoss.compareTo(BigDecimal.ZERO));
    assertEquals(500, stockPosition.getTotalQuantity());
  }

  @Test
  void testSellStocksMoreThanAvailableThrowsException() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        stockPosition.sellStocks(1500, new BigDecimal("15.00"));
      }
    );

    // Position did not change
    assertEquals(1000, stockPosition.getTotalQuantity());
  }

  @Test
  void testSellAllStocks() {
    stockPosition.addStocks(1000, new BigDecimal("10.00"));

    BigDecimal profitOrLoss = stockPosition.sellStocks(
      1000,
      new BigDecimal("15.00")
    );

    assertEquals(new BigDecimal("5000.00"), profitOrLoss);
    assertEquals(0, stockPosition.getTotalQuantity());
    assertFalse(stockPosition.hasStocks());
  }

  @Test
  void testAddLoss() {
    stockPosition.addLoss(new BigDecimal("1000.00"));
    assertEquals(
      new BigDecimal("1000.00"),
      stockPosition.getAccumulatedLosses()
    );

    stockPosition.addLoss(new BigDecimal("500.00"));
    assertEquals(
      new BigDecimal("1500.00"),
      stockPosition.getAccumulatedLosses()
    );
  }

  @Test
  void testReduceLosses() {
    stockPosition.addLoss(new BigDecimal("1000.00"));

    stockPosition.reduceLosses(new BigDecimal("300.00"));
    assertEquals(
      new BigDecimal("700.00"),
      stockPosition.getAccumulatedLosses()
    );
  }

  @Test
  void testReduceLossesBelowZeroBecomesZero() {
    stockPosition.addLoss(new BigDecimal("1000.00"));

    stockPosition.reduceLosses(new BigDecimal("1500.00"));
    assertEquals(0, stockPosition.getAccumulatedLosses().compareTo(BigDecimal.ZERO));
  }

  @Test
  void testReduceLossesFromZeroRemainsZero() {
    stockPosition.reduceLosses(new BigDecimal("500.00"));
    assertEquals(0, stockPosition.getAccumulatedLosses().compareTo(BigDecimal.ZERO));
  }

  @Test
  void testHasStocks() {
    assertFalse(stockPosition.hasStocks());

    stockPosition.addStocks(1000, new BigDecimal("10.00"));
    assertTrue(stockPosition.hasStocks());

    stockPosition.sellStocks(1000, new BigDecimal("15.00"));
    assertFalse(stockPosition.hasStocks());
  }

  @Test
  void testComplexScenarioMultipleBuysAndSells() {
    // Buy 1000,  $10 each
    stockPosition.addStocks(1000, new BigDecimal("10.00"));
    assertEquals(1000, stockPosition.getTotalQuantity());
    assertEquals(
      new BigDecimal("10.00"),
      stockPosition.getWeightedAveragePrice()
    );

    // Buy 2000, $20 -> weighted avg = 16.67
    stockPosition.addStocks(2000, new BigDecimal("20.00"));
    assertEquals(3000, stockPosition.getTotalQuantity());
    assertEquals(
      new BigDecimal("16.67"),
      stockPosition.getWeightedAveragePrice()
    );

    // Sell 1500, $15 -> loss of 2500 (1500 * (15 - 16.67))
    BigDecimal profitOrLoss = stockPosition.sellStocks(
      1500,
      new BigDecimal("15.00")
    );
    assertEquals(new BigDecimal("-2505.00"), profitOrLoss);
    assertEquals(1500, stockPosition.getTotalQuantity());

    // Add loss
    stockPosition.addLoss(new BigDecimal("2505.00"));
    assertEquals(
      new BigDecimal("2505.00"),
      stockPosition.getAccumulatedLosses()
    );
  }
}
