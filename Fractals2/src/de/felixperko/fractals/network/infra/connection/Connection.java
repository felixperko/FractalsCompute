package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.common.INetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;

public interface Connection<N extends INetworkManager> {
	public void writeMessage(Message msg);

	
	public N getNetworkManager();

	public SenderInfo getClientInfo();
	public void setClientInfo(SenderInfo clientInfo);
	
	public boolean isClosed();
	public void setClosed();
	
	public void addConnectionListener(ConnectionListener listener);
	public void removeConnectionListener(ConnectionListener listener);
}
