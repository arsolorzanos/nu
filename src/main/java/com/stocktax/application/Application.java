package com.stocktax.application;

import com.stocktax.domain.model.Operation;
import com.stocktax.domain.model.TaxCalculation;
import com.stocktax.domain.ports.InputPort;
import com.stocktax.domain.ports.OutputPort;
import com.stocktax.infrastructure.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private final TaxCalculationService taxCalculationService;
	private final InputPort inputAdapter;
	private final OutputPort outputAdapter;

	public Application(TaxCalculationService taxCalculationService, InputPort inputAdapter,
			OutputPort outputAdapter) {
		this.taxCalculationService = taxCalculationService;
		this.inputAdapter = inputAdapter;
		this.outputAdapter = outputAdapter;
	}

	public static void main(String[] args) {
		try {
			TaxCalculationService taxService = ApplicationConfig.createTaxCalculationService();
			InputPort inputAdapter = ApplicationConfig.createInputAdapter();
			OutputPort outputAdapter = ApplicationConfig.createOutputAdapter();

			Application app = new Application(taxService, inputAdapter,
					outputAdapter);

			app.run();

		} catch (Exception e) {
			logger.error("Application failed", e);
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	public void run() throws IOException {
		List<List<Operation>> allOperations = inputAdapter.readOperations();

		if (allOperations.isEmpty()) {
			logger.warn("No operations provided in input");
			return;
		}

		for (int i = 0; i < allOperations.size(); i++) {
			List<Operation> operations = allOperations.get(i);
			try {
				List<TaxCalculation> taxCalculations = taxCalculationService.calculateTaxes(operations);
				outputAdapter.writeTaxCalculations(taxCalculations);
			} catch (Exception e) {
				logger.error("Error processing operation set {}", i + 1, e);
				throw new IOException("Failed to process operation set " + (i + 1), e);
			}
		}
	}
}
