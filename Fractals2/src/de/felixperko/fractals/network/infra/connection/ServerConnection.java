package de.felixperko.fractals.network.infra.connection;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.data.shareddata.MappedSharedData;
import de.felixperko.fractals.manager.common.INetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.interfaces.Messageable;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;

/**
 * connection to the server
 */
public class ServerConnection extends AbstractConnection<INetworkManager>{
	
	Messageable writeToServer;
	
	INetworkManager networkManager;
	
	public MappedSharedData<TaskStateUpdate> stateUpdates = new MappedSharedData<>("remoteTaskStates", 0.05, true); //TODO refactor in separate class
	
	boolean closed = false;
	
	SenderInfo clientInfo;
	
	public ServerConnection(INetworkManager networkManager){
		this.networkManager = networkManager;
	}
	
	public Messageable getWriteToServer() {
		return writeToServer;
	}

	public void setWriteToServer(Messageable writeToServer) {
		this.writeToServer = writeToServer;
	}

	@Override
	public void writeMessage(Message msg) {
		if (writeToServer == null)
			throw new IllegalStateException("Attempted to write a message but the 'write to server' adapter wasn't set");
		writeToServer.writeMessage(msg);
	}

	
	@Override
	public INetworkManager getNetworkManager() {
		return networkManager;
	}
	
	@Override
	public void setClientInfo(SenderInfo clientInfo) {
		this.clientInfo = clientInfo;
	}
	
	@Override
	public SenderInfo getClientInfo() {
		return clientInfo;
	}
	
	public List<DataContainer> getSharedDataUpdates(Connection connection){
		List<DataContainer> list = new ArrayList<>();
		stateUpdates.getUpdatesAppendList(connection, list);
		return list;
	}
}
