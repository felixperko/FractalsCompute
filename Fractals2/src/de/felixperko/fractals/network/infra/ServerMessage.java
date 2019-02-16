package de.felixperko.fractals.network.infra;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;

public abstract class ServerMessage extends Message<ClientConnection, ServerConnection>{
	
	private static final long serialVersionUID = 7755283976684466284L;
	
	ClientConnection connection;
	ServerConnection backConnection;
	
	public ServerMessage() {
		super();
	}
	
	public ServerMessage(SenderInfo sender, Message<?, ?> lastMessage) {
		super(sender, lastMessage);
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
}
