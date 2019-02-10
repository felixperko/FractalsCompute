package de.felixperko.fractals.network;

import de.felixperko.fractals.manager.ClientNetworkManager;
import de.felixperko.fractals.manager.Manager;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public interface ClientConnection extends Connection<ClientNetworkManager> {
	public CalcSystem getCurrentSystem();
	public void setCurrentSystem(CalcSystem system);
	@Override
	ClientNetworkManager getNetworkManager();
}
