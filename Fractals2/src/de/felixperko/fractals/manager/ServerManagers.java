package de.felixperko.fractals.manager;

public class ServerManagers implements Managers {
	
	SystemManager systemManager;
	ServerThreadManager threadManager;
	NetworkManager networkManager;
	
	public ServerManagers() {
		systemManager = new SystemManager(this);
		threadManager = new ServerThreadManager(this);
		networkManager = new ServerNetworkManager(this);
	}
	
	public SystemManager getSystemManager() {
		return systemManager;
	}
	
	@Override
	public ServerThreadManager getThreadManager() {
		return threadManager;
	}
	
	@Override
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public ServerNetworkManager getServerNetworkManager() {
		return (ServerNetworkManager) networkManager;
	}
	
	
}
