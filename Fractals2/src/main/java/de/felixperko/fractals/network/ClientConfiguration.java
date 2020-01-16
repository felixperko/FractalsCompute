package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ClientConfiguration implements Serializable{
	
	private static final long serialVersionUID = -5403520207176226669L;
	
	Map<UUID, ParamContainer> instances = new HashMap<>();
	List<ParamContainer> systemRequests = new ArrayList<>();
	transient ClientConnection connectionToClient;
	
	public ClientConfiguration() {
	}
	
	public ClientConfiguration(ClientConfiguration cloneConfig, boolean newInstances){
		for (Entry<UUID, ParamContainer> e : cloneConfig.instances.entrySet()){
			this.instances.put(e.getKey(), new ParamContainer(e.getValue(), newInstances));
		}
		for (ParamContainer scd : cloneConfig.systemRequests){
			this.systemRequests.add(new ParamContainer(scd, newInstances));
		}
		this.connectionToClient = cloneConfig.connectionToClient;
	}
	
	public ClientConnection getConnection() {
		return connectionToClient;
	}

	public void setConnection(ClientConnection connectionToClient) {
		this.connectionToClient = connectionToClient;
	}
	
	public Map<UUID, ParamContainer> getParamContainers() {
		return instances;
	}
	
	public ParamContainer getParamContainer(UUID systemId) {
		return instances.get(systemId);
	}
	
	public void setParamContainer(UUID systemId, ParamContainer paramContainer){
		this.instances.put(systemId, paramContainer);
	}
	
	public ParamSupplier getParameter(UUID systemId, String name) {
		ParamContainer data = instances.get(systemId);
		return data.getClientParameter(name);
	}
	
	public <T> T getParameterGeneralValue(UUID systemId, String name, Class<T> cls) {
		return getParameter(systemId, name).get(null, cls, null, 0, 0);
	}
	
	public List<ParamContainer> getSystemRequests(){
		return systemRequests;
	}

	public void addRequest(ParamContainer systemClientData) {
		systemRequests.add(systemClientData);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connectionToClient == null) ? 0 : connectionToClient.hashCode());
		result = prime * result + ((instances == null) ? 0 : instances.hashCode());
		result = prime * result + ((systemRequests == null) ? 0 : systemRequests.hashCode());
		return result;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientConfiguration other = (ClientConfiguration) obj;
		if (connectionToClient == null) {
			if (other.connectionToClient != null)
				return false;
		} else if (!connectionToClient.equals(other.connectionToClient))
			return false;
		if (instances == null) {
			if (other.instances != null)
				return false;
		} else if (!instances.equals(other.instances))
			return false;
		if (systemRequests == null) {
			if (other.systemRequests != null)
				return false;
		} else if (!systemRequests.equals(other.systemRequests))
			return false;
		return true;
	}
}
