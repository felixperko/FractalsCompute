package de.felixperko.fractals.network.infra.connection;

import java.awt.Color;

import de.felixperko.fractals.manager.client.ClientNetworkManager;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

public class ClientLocalConnection extends AbstractConnection<ServerNetworkManager> implements ClientConnection{
	
	SenderInfo senderInfo;
	
	CategoryLogger log = new CategoryLogger("com/local", ColorContainer.MAGENTA);
	
	ServerNetworkManager networkManager;
	
	boolean closed;
	
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
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void setClosed() {
		this.closed = true;
	}

}
