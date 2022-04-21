package de.felixperko.fractals.network.infra.connection;

public interface ConnectionListener {
	public void connectionClosed(Connection<?> connection);
}
