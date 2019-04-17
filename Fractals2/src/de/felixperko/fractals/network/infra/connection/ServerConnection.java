package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.client.ClientNetworkManager;
import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.ClientWriteThread;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;

/**
 * connection to the server
 */
public class ServerConnection extends AbstractConnection<ClientNetworkManager>{
	
	ClientWriteThread writeToServer;
	
	ClientNetworkManager networkManager;
	
	boolean closed = false;
	
	public ServerConnection(ClientNetworkManager networkManager){
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
	public SenderInfo getSenderInfo() {
		throw new IllegalArgumentException("ServerConnection has no SenderInfo");
	}

	@Override
	public void setSenderInfo(SenderInfo clientInfo) {
		throw new IllegalArgumentException("ServerConnection has no SenderInfo");
	}

	
	@Override
	public ClientNetworkManager getNetworkManager() {
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
}
