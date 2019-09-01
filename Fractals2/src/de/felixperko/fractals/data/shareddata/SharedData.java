package de.felixperko.fractals.data.shareddata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.network.infra.connection.ConnectionListener;

public abstract class SharedData<U extends SharedDataUpdate> implements ConnectionListener{
	
	Map<Connection<?>, Integer> connections = new HashMap<>();
	
	final String dataIdentifier;
	
	public SharedData(String dataIdentifier) {
		this.dataIdentifier = dataIdentifier;
	}
	
	public abstract DataContainer getUpdates(Connection<?> connection);
	public abstract void update(U update);
	public abstract int getVersion();
	public abstract boolean isEmpty();
	
	public void getUpdatesAppendList(Connection connection, List<DataContainer> list) {
		DataContainer container = getUpdates(connection);
		if (container != null)
			list.add(container);
	}
	
	public boolean hasUpdate(Connection<?> connection) {
		if (isEmpty())
			return false;
		Integer version = connections.get(connection);
		return version == null || version < getVersion();
	}
	
	@Override
	public void connectionClosed(Connection<?> connection) {
		connections.remove(connection);
	}
	
	public String getDataIdentifier() {
		return dataIdentifier;
	}
}
