package de.felixperko.fractals.manager.client;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.common.ThreadManager;
import de.felixperko.fractals.network.ClientMessageInterface;

public class ClientManagers implements Managers {
	
	ThreadManager threadManager;
	ClientNetworkManager networkManager;
	
	public ClientManagers(ClientMessageInterface clientMessageInterface) {
		this.threadManager = new ClientThreadManager(this);
		this.networkManager = new ClientNetworkManager(this, clientMessageInterface);
	}

	@Override
	public ThreadManager getThreadManager() {
		return threadManager;
	}

	@Override
	public ClientNetworkManager getNetworkManager() {
		return networkManager;
	}

	public ClientNetworkManager getClientNetworkManager() {
		return networkManager;
	}

}