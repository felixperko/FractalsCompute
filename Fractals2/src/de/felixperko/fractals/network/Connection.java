package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.NetworkManager;

public interface Connection {
	public void writeMessage(Message msg);

	public SenderInfo getSenderInfo();
	
	public NetworkManager getNetworkManager();

	public void setSenderInfo(SenderInfo clientInfo);
}
