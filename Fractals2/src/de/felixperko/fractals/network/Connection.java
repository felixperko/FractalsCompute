package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.ConnectionListener;

public interface Connection<N extends NetworkManager> {
	public void writeMessage(Message msg);

	
	public N getNetworkManager();

	public SenderInfo getClientInfo();
	public void setClientInfo(SenderInfo clientInfo);
	
	public boolean isClosed();
	public void setClosed();
	
	public void addConnectionListener(ConnectionListener listener);
	public void removeConnectionListener(ConnectionListener listener);
}
