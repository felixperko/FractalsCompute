package de.felixperko.fractals.manager.client;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.manager.common.ThreadManager;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;
import de.felixperko.fractals.network.interfaces.NetworkInterfaceFactory;

public class ClientManagers implements Managers {
	
	ThreadManager threadManager;
	NetworkManager networkManager;
	
	public ClientManagers(NetworkInterfaceFactory networkInterfaceFactory) {
		this.threadManager = new ClientThreadManager(this);
		this.networkManager = new NetworkManager(this, networkInterfaceFactory);
	}

	@Override
	public ThreadManager getThreadManager() {
		return threadManager;
	}

	@Override
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public NetworkManager getClientNetworkManager() {
		return networkManager;
	}

}
