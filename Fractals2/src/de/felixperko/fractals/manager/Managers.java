package de.felixperko.fractals.manager;

public class Managers {
	
	SystemManager systemManager;
	ThreadManager threadManager;
	ServerNetworkManager networkManager;
	
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
	
	public ServerNetworkManager getNetworkManager() {
		return networkManager;
	}
	
	
}
