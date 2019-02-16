package de.felixperko.fractals.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ClientMessageInterface {
	
	Map<UUID, ClientSystemInterface> systemInterfaces = new HashMap<>();
	
	protected abstract ClientSystemInterface createSystemInterface(ClientConfiguration clientConfiguration);
	
	public ClientSystemInterface getSystemInterface(UUID systemId) {
		return systemInterfaces.get(systemId);
	}
	
	public void createdSystem(UUID systemId, ClientConfiguration clientConfiguration) {
		addSystemInterface(systemId, createSystemInterface(clientConfiguration));
	}
	
	public void removedSystem(UUID systemId) {
		removeSystemInterface(systemId);
	}
	
	protected void addSystemInterface(UUID systemId, ClientSystemInterface clientInterface) {
		systemInterfaces.put(systemId, clientInterface);
	}
	
	protected boolean removeSystemInterface(UUID systemId) {
		if (!systemInterfaces.containsKey(systemId))
			return false;
		systemInterfaces.remove(systemId);
		return true;
	}
}
