package de.felixperko.fractals.network.infra.connection;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.manager.common.INetworkManager;
import de.felixperko.fractals.network.Connection;

public abstract class AbstractConnection<N extends INetworkManager> implements Connection<N> {
	
	List<ConnectionListener> listeners = new ArrayList<>();

	boolean closed = false;
	
	public void addConnectionListener(ConnectionListener listener) {
		listeners.add(listener);
	}
	
	public void removeConnectionListener(ConnectionListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public void setClosed() {
		this.closed = true;
		listeners.forEach(l -> l.connectionClosed(this));
	}
}
