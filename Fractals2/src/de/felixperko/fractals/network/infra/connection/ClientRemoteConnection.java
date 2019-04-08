package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.client.ClientNetworkManager;
import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public class ClientRemoteConnection implements ClientConnection{
	
	SenderInfo info;
	ServerWriteThread writeThread;
	ServerNetworkManager networkManager;
	CalcSystem currentSystem;
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
	public CalcSystem getCurrentSystem() {
		return currentSystem;
	}
	
	@Override
	public void setCurrentSystem(CalcSystem system) {
		this.currentSystem = system;
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
