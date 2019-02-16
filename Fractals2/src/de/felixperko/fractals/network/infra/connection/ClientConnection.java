package de.felixperko.fractals.network.infra.connection;

import de.felixperko.fractals.manager.ClientNetworkManager;
import de.felixperko.fractals.manager.Manager;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public interface ClientConnection extends Connection<ServerNetworkManager> {
	public CalcSystem getCurrentSystem();
	public void setCurrentSystem(CalcSystem system);
	@Override
	ServerNetworkManager getNetworkManager();
}
