package de.felixperko.fractals.network.interfaces;

import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

/**
 * Interface to server for local connections.
 * setServerConnection() has to be called before writing any messages.
 */
public class ServerLocalMessageable implements Messageable{
	
	CategoryLogger log = new CategoryLogger("com/toLocalServer", new ColorContainer(1f, 0, 1));
	
	ServerConnection serverConnection;
	boolean closeConnection;
	
	public void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}
	
	@Override
	public Connection<?> getConnection() {
		return serverConnection;
	}

	@Override
	public void closeConnection() {
		this.closeConnection = true;
		serverConnection.setClosed();
	}

	@Override
	public boolean isCloseConnection() {
		return closeConnection;
	}

	@Override
	public void prepareMessage(Message msg) {
		msg.setSentTime(System.nanoTime());
	}

	@Override
	public void writeMessage(Message msg) {
		msg.received(serverConnection, log);
	}
}
