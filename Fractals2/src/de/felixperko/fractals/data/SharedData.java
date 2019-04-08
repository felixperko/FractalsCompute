package de.felixperko.fractals.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.infra.connection.ClientConnection;

/**
 * Continuous data that needs to be synced for clients through update messages.
 */
public abstract class SharedData {
	int versionCounter = 0;
	int disposedCounter = 0;
	
	Map<Integer, List<SharedDataUpdate>> updates = new HashMap<>();
	
	Map<Connection<?>, Integer> connections = new HashMap<>();
	
	
	
	List<Connection<?>> removeList = new ArrayList<>();
	
	public SharedData() {
		
	}
	
	public SharedDataContainer getUpdates(Connection connection){
		if (!connections.containsKey(connection))
			connections.put(connection, (Integer)(-1));
		
		int currentVersion = connections.get(connection);
		if (currentVersion == versionCounter)
			return null;
		
		List<SharedDataUpdate> list = new ArrayList<>();
		for (int i = Math.max(currentVersion, 0) ; i < versionCounter ; i++) {
			list.addAll(updates.get(i));
		}
		SharedDataContainer ans;
		synchronized (updates) {
			list.addAll(updates.get(versionCounter));
			ans = new SharedDataContainer(versionCounter, list);
			connections.put(connection, (Integer)versionCounter);
			if (!list.isEmpty())
				versionCounter++;
			
			int lowestDistributedVersion = versionCounter;
			
			for (Entry<Connection<?>, Integer> e : connections.entrySet()) {
				if (e.getKey().isClosed())
					removeList.add(e.getKey());
				else {
					int version = e.getValue();
					if (version < lowestDistributedVersion)
						lowestDistributedVersion = version;
				}
			}
			
			removeList.forEach(c -> connections.remove(c));
			removeList.clear();

			for (int i = disposedCounter ; i < lowestDistributedVersion ; i++) {
				updates.remove((Integer)i);
			}
			
		}
		
		
		return ans;
	}
	
	public void update(SharedDataUpdate update) {
		synchronized (updates) {
		}
	}
}
