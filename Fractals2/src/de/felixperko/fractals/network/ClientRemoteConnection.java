package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public class ClientRemoteConnection implements ClientConnection{
	
	SenderInfo info;
	ServerWriteThread writeThread;
	ServerNetworkManager networkManager;
	CalcSystem currentSystem;
	
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


}
