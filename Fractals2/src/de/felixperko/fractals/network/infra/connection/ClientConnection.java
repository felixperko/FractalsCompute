package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.Connection;

public interface ClientConnection extends Connection<ServerNetworkManager> {
	public ServerNetworkManager getNetworkManager();
}
