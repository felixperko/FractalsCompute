package de.felixperko.fractals.network.messages;

import java.util.List;

import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;

public class ReachableResponseMessage extends ClientMessage{

	private static final long serialVersionUID = 9074769840423925117L;
	
	List<DataContainer> sharedDataStateUpdates;

	public ReachableResponseMessage(ServerConnection connection) {
		addSentCallback(new Runnable() {
			@Override
			public void run() {
				sharedDataStateUpdates = connection.getSharedDataUpdates(connection);
			}
		});
	}
	
	@Override
	protected void process() {
		if (sharedDataStateUpdates != null && !sharedDataStateUpdates.isEmpty()){
			NetworkManager networkManager = getBackConnection().getNetworkManager();
			ServerConnection serverConnection = networkManager.getServerConnection(getBackConnection().getClientInfo());
			if (serverConnection != null)
				networkManager.getMessageInterface(serverConnection).sharedDataUpdated(sharedDataStateUpdates);
		}
	}

}
