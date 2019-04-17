package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.infra.connection.AbstractConnection;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.infra.connection.ConnectionListener;

/**
 * Continuous data that needs to be synced for clients through update messages.
 */
public abstract class ContinuousSharedData implements ConnectionListener{
	int versionCounter = 0;
	int disposedCounter = 0;
	
	boolean disposeDistributed = true;
	
	Map<Integer, List<SharedDataUpdate>> updates = new HashMap<>();
	
	Map<Connection<?>, Integer> connections = new HashMap<>();
	
	public ContinuousSharedData() {
		
	}
	
	public SharedDataContainer getUpdates(Connection<?> connection){
		
		//prepare state
		if (!connections.containsKey(connection))
			connections.put(connection, (Integer)(-1));
		int currentVersion = connections.get(connection);
		if (currentVersion == versionCounter)
			return null;
		
		//accumulate past version updates
		List<SharedDataUpdate> list = new ArrayList<>();
		for (int i = Math.max(currentVersion, 0) ; i < versionCounter ; i++) {
			list.addAll(updates.get(i));
		}
		
		//add current version updates and increment version
		SharedDataContainer ans;
		synchronized (updates) {
			list.addAll(updates.get(versionCounter));
			ans = new SharedDataContainer(versionCounter, list);
			connections.put(connection, (Integer)versionCounter);
			if (!list.isEmpty())
				versionCounter++;
			
			//dispose data that is already distributed to all registered clients
			if (disposeDistributed) {
				int lowestDistributedVersion = versionCounter;
				for (Entry<Connection<?>, Integer> e : connections.entrySet()) {
					int version = e.getValue();
					if (version < lowestDistributedVersion)
						lowestDistributedVersion = version;
				}
				
				for (int i = disposedCounter ; i <= lowestDistributedVersion ; i++) {
					updates.remove((Integer)i);
				}
				
				disposedCounter = lowestDistributedVersion;
			}
			
		}
		
		
		return ans;
	}
	
	public void update(SharedDataUpdate update) {
		synchronized (updates) {
			updates.get(versionCounter).add(update);
		}
	}

	public boolean isDisposeDistributed() {
		return disposeDistributed;
	}

	public void setDisposeDistributed(boolean disposeDistributed) {
		this.disposeDistributed = disposeDistributed;
	}
	
	@Override
	public void connectionClosed(Connection<?> connection) {
		synchronized(updates) {
			connections.remove(connection);
		}
	}
}
