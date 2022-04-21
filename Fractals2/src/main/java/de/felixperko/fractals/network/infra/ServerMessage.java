package de.felixperko.fractals.network.infra;

import org.slf4j.Logger;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;

public abstract class ServerMessage extends Message<ClientConnection, ServerConnection>{
	
	private static final long serialVersionUID = 7755283976684466284L;
	
	ClientConnection connection;
	ServerConnection backConnection;
	ClientMessageInterface clientMessageInterface;
	
	public ServerMessage() {
		super();
	}
	
	public ServerMessage(SenderInfo sender, Message<?, ?> lastMessage) {
		super(sender, lastMessage);
	}
	
	@Override
	public void received(ServerConnection connection, Logger log) {
		clientMessageInterface = connection.getNetworkManager().getMessageInterface(connection);
		super.received(connection, log);
	}
	
	@Override
	public ClientConnection getConnection() {
		return connection;
	}

	@Override
	protected void setConnection(ClientConnection connection) {
		this.connection = connection;
	}

	public ServerConnection getBackConnection() {
		return backConnection;
	}

	public void setBackConnection(ServerConnection backConnection) {
		this.backConnection = backConnection;
	}

	public ClientMessageInterface getClientMessageInterface() {
		return clientMessageInterface;
	}
}
