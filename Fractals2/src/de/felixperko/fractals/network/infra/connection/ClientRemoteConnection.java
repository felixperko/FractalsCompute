package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.network.infra.Message;

public class ClientRemoteConnection extends AbstractConnection<ServerNetworkManager> implements ClientConnection{
	
	SenderInfo info;
	ServerWriteThread writeThread;
	ServerNetworkManager networkManager;
	boolean closed;
	
	public ClientRemoteConnection(ServerNetworkManager networkManager, SenderInfo info, ServerWriteThread writeThread) {
		this.networkManager = networkManager;
		this.info = info;
		this.writeThread = writeThread;
	}
	
	@Override
	public void writeMessage(Message msg) {
		writeThread.writeMessage(msg);
	}

	public SenderInfo getSenderInfo() {
		return info;
	}

	@Override
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
	}

	@Override
	public void setSenderInfo(SenderInfo clientInfo) {
		this.info = clientInfo;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
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
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		return true;
	}
	
}
