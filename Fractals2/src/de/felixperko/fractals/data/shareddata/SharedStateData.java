package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.network.Connection;

/**
 * Shares the latest state update between clients
 */
public class SharedStateData<T extends SharedStateUpdate> extends SharedData<T> {
	
	int versionCounter = 0;
	SharedStateUpdate currentState;
	
	public SharedStateData(String dataIdentifier) {
		super(dataIdentifier);
	}
	
	@Override
	public synchronized DataContainer getUpdates(Connection<?> connection) {
		
		Integer version = connections.get(connection);
		if (version == null) {
			version = 0;
			connections.put(connection, version);
		}
		if (version == versionCounter)
			return null;
		
		List<SharedDataUpdate> list = new ArrayList<SharedDataUpdate>();
		currentState.setSent();
		list.add(currentState);
		return new DataContainer(dataIdentifier, list);
	}

	@Override
	public synchronized void update(SharedStateUpdate update) {
		this.currentState = update;
		versionCounter++;
	}
	
	@Override
	public void connectionClosed(Connection<?> connection) {
		synchronized (this) {
			super.connectionClosed(connection);
		}
	}

	@Override
	public int getVersion() {
		return versionCounter;
	}

	@Override
	public boolean isEmpty() {
		return versionCounter == 0;
	}

}
