package de.felixperko.fractals;

import de.felixperko.fractals.manager.server.ServerManagers;

public class FractalsMain {
	
	static ServerManagers managers;
	
	public static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	public static int THREAD_COUNT = 2;
	
	public static void main(String[] args) {
		
		managers = new ServerManagers();

		managers.getSystemManager().insertAvailableSystems();
		
		managers.getThreadManager().startWorkerThreads(THREAD_COUNT);
		managers.getServerNetworkManager().startServerConnectThread();
		
	}
}
