package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientConfiguration implements Serializable{
	
	private static final long serialVersionUID = -5403520207176226669L;
	
	Map<UUID, SystemInstanceClientData> instances = new HashMap<>();
	List<SystemInstanceClientData> systemRequests = new ArrayList
	transient ClientConnection connectionToClient;
	
	public ClientConfiguration(ClientConnection clientConnection) {
		this.connectionToClient = clientConnection;
	}
	
	public ClientConnection getConnection() {
		return connectionToClient;
	}

	public void setConnection(ClientConnection connectionToClient) {
		this.connectionToClient = connectionToClient;
	}

	
	public Map<UUID, SystemInstanceClientData> getSystemClientData() {
		return instances;
	}
	
	public SystemInstanceClientData getSystemClientData(UUID systemId) {
		return instances.get(systemId);
	}
}
