package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.client.ClientNetworkManager;
import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public interface ClientConnection extends Connection<ServerNetworkManager> {
	public ServerNetworkManager getNetworkManager();
}
