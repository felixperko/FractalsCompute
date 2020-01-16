package de.felixperko.fractals.network.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.task.FractalsTask;

public abstract class ClientMessageInterface {
	
	Map<UUID, ClientSystemInterface> systemInterfaces = new HashMap<>();
	ServerConnection serverConnection;
	
	public ClientMessageInterface(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	protected abstract ClientSystemInterface createSystemInterface(ClientConfiguration clientConfiguration);
	
	public Set<UUID> getRegisteredSystems(){
		return systemInterfaces.keySet();
	}
	
	public ClientSystemInterface getSystemInterface(UUID systemId) {
		return systemInterfaces.get(systemId);
	}
	
	public void createdSystem(UUID systemId, ClientConfiguration clientConfiguration, ParameterConfiguration parameterConfiguration) {
		ClientSystemInterface systemInterface = createSystemInterface(clientConfiguration);
		addSystemInterface(systemId, systemInterface);
		systemInterface.updateParameterConfiguration(clientConfiguration.getParamContainer(systemId), parameterConfiguration);
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
	
	public abstract void serverStateUpdated(ServerStateInfo serverStateInfo);
	
	public abstract void assignedTasks(List<FractalsTask> tasks);

	public abstract void updateSharedData(DataContainer container);

	public void sharedDataUpdated(List<DataContainer> sharedDataStateUpdates) {
		for (DataContainer dataContainer : sharedDataStateUpdates)
			updateSharedData(dataContainer);
	}
	
	public ServerConnection getServerConnection() {
		return serverConnection;
	}
}
