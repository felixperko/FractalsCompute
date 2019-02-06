package de.felixperko.fractals.manager;

public class Managers {
	
	SystemManager systemManager;
	ThreadManager threadManager;
	NetworkManager networkManager;
	
	public Managers() {
		systemManager = new SystemManager(this);
		threadManager = new ThreadManager(this);
		networkManager = new NetworkManager(this);
	}
	
	public SystemManager getSystemManager() {
		return systemManager;
	}
	
	public ThreadManager getThreadManager() {
		return threadManager;
	}
	
	public NetworkManager getNetworkManager() {
		return networkManager;
	}
	
	
}
