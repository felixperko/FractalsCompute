package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.ServerNetworkManager;

public interface Connection {
	public void writeMessage(Message msg);

	public SenderInfo getSenderInfo();
	
	public ServerNetworkManager getNetworkManager();

	public void setSenderInfo(SenderInfo clientInfo);
}
