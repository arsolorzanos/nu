package com.stocktax.infrastructure.config;

import com.stocktax.application.TaxCalculationService;
import com.stocktax.domain.TaxCalculator;
import com.stocktax.domain.ports.InputPort;
import com.stocktax.domain.ports.OutputPort;
import com.stocktax.infrastructure.adapters.JsonInputAdapter;
import com.stocktax.infrastructure.adapters.JsonOutputAdapter;


public class ApplicationConfig {
 
	public static TaxCalculationService createTaxCalculationService() {
		TaxCalculator taxCalculator = new TaxCalculator();
		return new TaxCalculationService(taxCalculator);
	}

	public static InputPort createInputAdapter() {
			// we can later change this for new adapters
			return createJsonInputAdapter();
	}

	public static OutputPort createOutputAdapter() {
			// we can later change this for new adapters
			return createJsonOutputAdapter();
	}

	public static InputPort createJsonInputAdapter() {
		return new JsonInputAdapter();
	}

	public static OutputPort createJsonOutputAdapter() {
		return new JsonOutputAdapter();
	}
}
