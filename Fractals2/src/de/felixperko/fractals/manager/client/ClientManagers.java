package de.felixperko.fractals.manager.client;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.common.ThreadManager;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;
import de.felixperko.fractals.network.interfaces.NetworkInterfaceFactory;

public class ClientManagers implements Managers {
	
	ThreadManager threadManager;
	ClientNetworkManager networkManager;
	
	public ClientManagers(NetworkInterfaceFactory networkInterfaceFactory) {
		this.threadManager = new ClientThreadManager(this);
		this.networkManager = new ClientNetworkManager(this, networkInterfaceFactory);
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
