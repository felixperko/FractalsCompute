package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public class SystemClientData implements Serializable{
	
	private static final long serialVersionUID = -6322484739454792244L;
	
	Map<String, ParamSupplier> clientParameters;
	int grantThreads;
	
	public SystemClientData(Map<String, ParamSupplier> clientParameters, int grantThreads) {
		this.clientParameters = clientParameters;
		this.grantThreads = grantThreads;
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
	
	public int getGrantedThreads() {
		return grantThreads;
	}

	
	public Map<String, ParamSupplier> getClientParameters() {
		return clientParameters;
	}
}
