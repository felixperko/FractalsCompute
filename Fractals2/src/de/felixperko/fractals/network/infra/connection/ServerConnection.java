package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.common.INetworkManager;
import de.felixperko.fractals.network.ClientWriteThread;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;

/**
 * connection to the server
 */
public class ServerConnection extends AbstractConnection<INetworkManager>{
	
	ClientWriteThread writeToServer;
	
	INetworkManager networkManager;
	
	boolean closed = false;
	
	SenderInfo clientInfo;
	
	public ServerConnection(INetworkManager networkManager){
		this.networkManager = networkManager;
	}
	
	public ClientWriteThread getWriteToServer() {
		return writeToServer;
	}

	public void setWriteToServer(ClientWriteThread writeToServer) {
		this.writeToServer = writeToServer;
	}

	@Override
	public void writeMessage(Message msg) {
		if (writeToServer == null)
			throw new IllegalStateException("Attempted to write a message but the 'write to server' thread wasn't set");
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
}
