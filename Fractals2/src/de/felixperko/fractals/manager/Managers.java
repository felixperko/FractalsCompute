package de.felixperko.fractals.manager;

public class Managers {
	
	SystemManager systemManager;
	ThreadManager threadManager;
	Manager networkManager;
	
	public Managers() {
		systemManager = new SystemManager(this);
		threadManager = new ThreadManager(this);
		networkManager = new ServerNetworkManager(this);
	}
	
	public SystemManager getSystemManager() {
		return systemManager;
	}
	
	public ThreadManager getThreadManager() {
		return threadManager;
	}
	
	public Manager getNetworkManager() {
		return networkManager;
	}

	public ServerNetworkManager getServerNetworkManager() {
		return (ServerNetworkManager) networkManager;
	}

	public ClientNetworkManager getClientNetworkManager() {
		return (ClientNetworkManager) networkManager;
	}
	
	
}
