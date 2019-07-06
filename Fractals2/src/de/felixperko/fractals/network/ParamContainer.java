package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ParamContainer implements Serializable{

	private static final long serialVersionUID = 2325163791938639608L;
	
	protected Map<String, ParamSupplier> clientParameters;

	public ParamContainer() {
	}

	public ParamContainer(Map<String, ParamSupplier> clientParameters) {
		this.clientParameters = clientParameters;
	}

	public void addClientParameter(ParamSupplier paramSupplier) {
		clientParameters.put(paramSupplier.getName(), paramSupplier);
	}

	public boolean hasClientParameters() {
		return clientParameters.size() > 0;
	}

	public ParamSupplier getClientParameter(String name) {
		return clientParameters.get(name);
	}

	public Map<String, ParamSupplier> getClientParameters() {
		return clientParameters;
	}

}