package com.stocktax.domain.ports;

import java.io.IOException;
import java.util.List;

import com.stocktax.domain.model.TaxCalculation;

public interface OutputPort {
	void writeTaxCalculations(List<TaxCalculation> taxCalculations) throws IOException;
}
