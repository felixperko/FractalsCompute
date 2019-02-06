package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.NetworkManager;

/**
 * connection to the server
 */
public class ServerConnection implements Connection{
	
	ClientWriteThread writeToServer;
	
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
	public NetworkManager getNetworkManager() {
		throw new IllegalArgumentException("ServerConnection has no NetworkManager");
	}
}
