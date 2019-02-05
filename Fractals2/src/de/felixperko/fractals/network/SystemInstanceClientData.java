package de.felixperko.fractals.network;

import java.util.HashMap;

import de.felixperko.fractals.system.parameters.ParamSupplier;

public class SystemInstanceClientData {
	HashMap<String, ParamSupplier> parameters = new HashMap<>();
	
	public void addParamSupplier(ParamSupplier paramSupplier) {
		parameters.put(paramSupplier.getName(), paramSupplier);
	}
}
