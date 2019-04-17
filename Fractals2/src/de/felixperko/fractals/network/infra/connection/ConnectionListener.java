package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.network.Connection;

public interface ConnectionListener {
	public void connectionClosed(Connection<?> connection);
}
