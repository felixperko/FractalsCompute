package de.felixperko.fractals.network.infra.connection;

import java.awt.Color;

import de.felixperko.fractals.manager.ClientNetworkManager;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.util.CategoryLogger;

public class ClientLocalConnection implements ClientConnection {
	
	SenderInfo senderInfo;
	
	CategoryLogger log = new CategoryLogger("com/local", Color.MAGENTA);
	
	ServerNetworkManager networkManager;
	
	CalcSystem currentSystem;
	
	public ClientLocalConnection(ServerNetworkManager networkManager, SenderInfo localSenderInfo) {
		this.networkManager = networkManager;
		this.senderInfo = localSenderInfo;
	}

	@Override
	public SenderInfo getSenderInfo() {
		return senderInfo;
	}

	@Override
	public void setSenderInfo(SenderInfo clientInfo) {
		this.senderInfo = clientInfo;
	}
	
	@Override
	public void writeMessage(Message msg) {
		msg.setSentTime();
		msg.received(this, log);
	}

	@Override
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
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
