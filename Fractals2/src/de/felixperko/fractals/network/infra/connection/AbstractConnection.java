package de.felixperko.fractals.network.infra.connection;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.Connection;

public abstract class AbstractConnection<N extends NetworkManager> implements Connection<N> {
	
	List<ConnectionListener> listeners = new ArrayList<>();
	
	public void addConnectionListener(ConnectionListener listener) {
		listeners.add(listener);
	}
	
	public void removeConnectionListener(ConnectionListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void setClosed() {
		listeners.forEach(l -> l.connectionClosed(this));
	}
}
