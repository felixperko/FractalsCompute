package de.felixperko.fractals.data.shareddata;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.infra.connection.ConnectionListener;

public abstract class SharedData<U extends SharedDataUpdate> implements ConnectionListener{
	
	Map<Connection<?>, Integer> connections = new HashMap<>();
	
	public abstract DataContainer getUpdates(Connection<?> connection);
	public abstract void update(U update);
	
	@Override
	public void connectionClosed(Connection<?> connection) {
		connections.remove(connection);
	}
}
