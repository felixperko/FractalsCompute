package de.felixperko.fractals.network.interfaces;

import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.Connection;

/**
 * Interface for message communication with another instance (server/client)
 */
public interface Messageable {

	Connection<?> getConnection();
	void setConnection(Connection<?> clientLocalConnection);
	void closeConnection();
	boolean isCloseConnection();
	
	void prepareMessage(Message msg);
	void writeMessage(Message msg);
	
	/**
	 * Called for initialization, e.g. start of thread if the implementation extends Thread
	 */
	void start();
}