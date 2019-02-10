package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.Manager;
import de.felixperko.fractals.manager.NetworkManager;
import de.felixperko.fractals.network.infra.Message;

public interface Connection<N extends NetworkManager> {
	public void writeMessage(Message msg);

	public SenderInfo getSenderInfo();
	
	public N getNetworkManager();

	public void setSenderInfo(SenderInfo clientInfo);
}
