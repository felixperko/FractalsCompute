package de.felixperko.fractals.network.infra;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ClientConnection;

public abstract class ClientMessage extends Message<ClientConnection, ClientConnection> {
	
	private static final long serialVersionUID = -6067480185642257683L;
	
	ClientConnection connection;
	ClientConnection backConnection;
	
	public ClientMessage() {
		super();
	}
	
	public ClientMessage(SenderInfo sender, Message<?, ?> lastMessage) {
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
	
	@Override
	public ClientConnection getBackConnection() {
		return backConnection;
	}
	
	@Override
	protected void setBackConnection(ClientConnection connection) {
		this.backConnection = connection;
	}

}
