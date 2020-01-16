package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.server.ServerNetworkManager;

public interface ClientConnection extends Connection<ServerNetworkManager> {
	public ServerNetworkManager getNetworkManager();
}
