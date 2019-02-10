package de.felixperko.fractals.network.infra;

import de.felixperko.fractals.network.ClientConnection;

public abstract class ServerMessage extends Message<ClientConnection>{
	
	private static final long serialVersionUID = 7755283976684466284L;
	
	ClientConnection connection;
	
	@Override
	public ClientConnection getConnection() {
		return connection;
	}

	@Override
	protected void setConnection(ClientConnection connection) {
		this.connection = connection;
	}
}
