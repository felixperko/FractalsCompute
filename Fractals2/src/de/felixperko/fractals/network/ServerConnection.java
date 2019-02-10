package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.ClientNetworkManager;
import de.felixperko.fractals.manager.Manager;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.network.infra.Message;

/**
 * connection to the server
 */
public class ServerConnection implements Connection<ServerNetworkManager>{
	
	ClientWriteThread writeToServer;
	
	ServerNetworkManager networkManager;
	
	public ServerConnection(ServerNetworkManager networkManager){
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
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
	}
}
