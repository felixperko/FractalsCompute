package de.felixperko.fractals.network.interfaces;

import java.util.List;
import java.util.UUID;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.data.shareddata.MappedSharedDataUpdate;
import de.felixperko.fractals.data.shareddata.SharedDataUpdate;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.RemoteTaskProvider;

/**
 * The ClientMessageInterface in case the client is a server as well
 */
public class ServerMessageInterface extends ClientMessageInterface {
	
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
		ServerNetworkManager serverNetworkManager = ((ServerNetworkManager)serverConnection.getNetworkManager());
		RemoteTaskProvider remoteTaskProvider = serverNetworkManager.getServerManagers().getThreadManager().getRemoteTaskProvider();
		remoteTaskProvider.addTasks(tasks, serverConnection);
	}

	@Override
	public void updateSharedData(DataContainer container) {
		for (SharedDataUpdate<?> sdu : container.getUpdates()){
			if (container.getIdentifier().equals("remoteTaskStates")){
				if (sdu instanceof MappedSharedDataUpdate<?>){
					@SuppressWarnings("unchecked")
					MappedSharedDataUpdate<TaskStateUpdate> msdu = (MappedSharedDataUpdate<TaskStateUpdate>) sdu;
					for (TaskStateUpdate update : msdu.getUpdates()){
						boolean updated = ((ServerManagers)serverConnection.getNetworkManager().getManagers()).getSystemManager().updateTaskState(update);
						if (!updated){
							System.err.println("Couldn't update TaskStateInfo");
							Thread.dumpStack();
						}
					}
				}
			}
		}
	}
}
