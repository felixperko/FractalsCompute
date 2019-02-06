package de.felixperko.fractals.network;

import java.util.HashMap;
import java.util.UUID;

import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public class SystemInstanceClientData {
	HashMap<String, ParamSupplier> clientParameters = new HashMap<>();
	int grantThreads;
	
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

	
	public HashMap<String, ParamSupplier> getClientParameters() {
		return clientParameters;
	}
}
