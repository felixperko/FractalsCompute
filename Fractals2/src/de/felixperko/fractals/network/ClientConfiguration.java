package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ClientConfiguration implements Serializable{
	
	private static final long serialVersionUID = -5403520207176226669L;
	
	Map<UUID, SystemClientData> instances = new HashMap<>();
	List<SystemClientData> systemRequests = new ArrayList<>();
	transient ClientConnection connectionToClient;
	
	public ClientConfiguration() {
	}
	
	public ClientConnection getConnection() {
		return connectionToClient;
	}

	public void setConnection(ClientConnection connectionToClient) {
		this.connectionToClient = connectionToClient;
	}
	
	public Map<UUID, SystemClientData> getSystemClientData() {
		return instances;
	}
	
	public SystemClientData getSystemClientData(UUID systemId) {
		return instances.get(systemId);
	}
	
	public ParamSupplier getParameter(UUID systemId, String name) {
		SystemClientData data = instances.get(systemId);
		return data.getClientParameter(name);
	}
	
	public <T> T getParameterGeneralValue(UUID systemId, String name, Class<T> cls) {
		return (T) getParameter(systemId, name).get(0, 0);
	}
	
	public List<SystemClientData> getSystemRequests(){
		return systemRequests;
	}

	public void addRequest(SystemClientData systemClientData) {
		systemRequests.add(systemClientData);
	}
}
