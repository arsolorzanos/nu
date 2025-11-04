package com.stocktax.domain.ports;

import java.io.IOException;
import java.util.List;

import com.stocktax.domain.model.Operation;

public interface InputPort {
	List<List<Operation>> readOperations() throws IOException;
}
