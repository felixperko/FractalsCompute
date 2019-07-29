package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.network.infra.Message;

public class ClientRemoteConnection extends AbstractConnection<ServerNetworkManager> implements ClientConnection{
	
	SenderInfo clientInfo;
	ServerWriteThread writeThread;
	ServerNetworkManager networkManager;
	boolean closed;
	
	public ClientRemoteConnection(ServerNetworkManager networkManager, SenderInfo info, ServerWriteThread writeThread) {
		this.networkManager = networkManager;
		this.clientInfo = info;
		this.writeThread = writeThread;
	}
	
	@Override
	public void writeMessage(Message msg) {
		writeThread.writeMessage(msg);
	}

	public SenderInfo getSenderInfo() {
		return clientInfo;
	}

	@Override
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
	}

	@Override
	public SenderInfo getClientInfo() {
		return clientInfo;
	}
	
	@Override
	public void setClientInfo(SenderInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientInfo == null) ? 0 : clientInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientRemoteConnection other = (ClientRemoteConnection) obj;
		if (clientInfo == null) {
			if (other.clientInfo != null)
				return false;
		} else if (!clientInfo.equals(other.clientInfo))
			return false;
		return true;
	}
	
}
