package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.client.ClientNetworkManager;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.ClientWriteThread;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;

/**
 * connection to the server
 */
public class ServerConnection extends AbstractConnection<NetworkManager>{
	
	ClientWriteThread writeToServer;
	
	NetworkManager networkManager;
	
	boolean closed = false;
	
	SenderInfo clientInfo;
	
	public ServerConnection(NetworkManager networkManager){
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
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	
	@Override
	public boolean isClosed() {
		return closed;
	}
	

	@Override
	public void setClosed() {
		closed = true;
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
