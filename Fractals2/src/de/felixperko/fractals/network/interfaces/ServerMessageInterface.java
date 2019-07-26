package de.felixperko.fractals.network.interfaces;

import java.util.List;
import java.util.UUID;

import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.RemoteTaskProvider;

public class ServerMessageInterface extends ClientMessageInterface {
	
	RemoteTaskProvider taskProvider;
	
	public ServerMessageInterface(ServerConnection serverConnection) {
		super(serverConnection);
	}

	@Override
	protected ClientSystemInterface createSystemInterface(ClientConfiguration clientConfiguration) {
		return null;
	}
	
	@Override
	public void createdSystem(UUID systemId, ClientConfiguration clientConfiguration,
			ParameterConfiguration parameterConfiguration) {
		throw new IllegalStateException("Server to Server connections shouldn't be system specific");
	}

	@Override
	public void serverStateUpdated(ServerStateInfo serverStateInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void assignedTasks(List<FractalsTask> tasks) {
		taskProvider.addTasks(tasks, serverConnection);
	}

	@Override
	public void updateSharedData(DataContainer container) {
		// TODO Auto-generated method stub
		
	}
}
