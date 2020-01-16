package de.felixperko.fractals.manager.server;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.network.interfaces.NetworkInterfaceFactory;
import de.felixperko.fractals.network.interfaces.ServerMessageInterface;

public class ServerManagers implements Managers {
	
	SystemManager systemManager;
	ServerThreadManager threadManager;
	ServerNetworkManager networkManager;
	
	public ServerManagers() {
		systemManager = new SystemManager(this);
		threadManager = new ServerThreadManager(this);
		networkManager = new ServerNetworkManager(this, new NetworkInterfaceFactory(ServerMessageInterface.class, null));
	}
	
	public SystemManager getSystemManager() {
		return systemManager;
	}
	
	@Override
	public ServerThreadManager getThreadManager() {
		return threadManager;
	}
	
	@Override
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
	}

	public ServerNetworkManager getServerNetworkManager() {
		return (ServerNetworkManager) networkManager;
	}
	
	
}
