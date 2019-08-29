package de.felixperko.fractals.network;

import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.Connection;

public interface ComAdapter {
	
	Connection<?> getConnection();
	void closeConnection();
	boolean isCloseConnection();
	
	void prepareMessage(Message msg);
	void writeMessage(Message msg);
}