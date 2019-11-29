package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.interfaces.Messageable;

public class ClientLocalConnection extends AbstractConnection<ServerNetworkManager> implements ClientConnection{
	
	SenderInfo senderInfo;
	
	ServerNetworkManager networkManager;
	
	ServerConnection serverConnection;
	
	Messageable clientMessageable;
	
	public ClientLocalConnection(ServerNetworkManager networkManager, SenderInfo localSenderInfo, Messageable clientMessageable) {
		this.networkManager = networkManager;
		this.senderInfo = localSenderInfo;
		this.clientMessageable = clientMessageable;
		this.clientMessageable.setConnection(this);
	}

	@Override
	public SenderInfo getClientInfo() {
		return senderInfo;
	}

	@Override
	public void setClientInfo(SenderInfo clientInfo) {
		this.senderInfo = clientInfo;
	}
	
	@Override
	public void writeMessage(Message msg) {
		msg.setSentTime();
		clientMessageable.writeMessage(msg);
	}

	@Override
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
	}

}
