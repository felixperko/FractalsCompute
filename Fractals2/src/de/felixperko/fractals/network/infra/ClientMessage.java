package de.felixperko.fractals.network.infra;

import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.ServerConnection;

public abstract class ClientMessage extends Message<ServerConnection> {
	
	private static final long serialVersionUID = -6067480185642257683L;
	
	ServerConnection connection;
	
	@Override
	public ServerConnection getConnection() {
		return connection;
	}

	@Override
	protected void setConnection(ServerConnection connection) {
		this.connection = connection;
	}

}
