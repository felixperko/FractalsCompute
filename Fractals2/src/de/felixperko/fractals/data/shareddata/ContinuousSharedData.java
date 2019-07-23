package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.network.Connection;

/**
 * Continuous data that needs to be synced for clients through update messages.
 * The data is divided in discrete SharedDataUpdate objects.
 */
public class ContinuousSharedData<T extends SharedDataUpdate> extends SharedData<T>{
	int versionCounter = 0;
	int disposedCounter = 0;
	
	boolean disposeDistributed = true;
	
	Map<Integer, List<SharedDataUpdate>> updates = new HashMap<>();
	
	public ContinuousSharedData(String dataIdentifier, boolean disposeDistributed) {
		super(dataIdentifier);
		this.disposeDistributed = disposeDistributed;
	}
	
	public DataContainer getUpdates(Connection<?> connection){
		
		//prepare state
		if (!connections.containsKey(connection))
			connections.put(connection, (Integer)(-1));
		int currentVersion = connections.get(connection);

		DataContainer ans;
		
		synchronized (this) {
			if (currentVersion == versionCounter || (currentVersion == versionCounter-1 && getUpdateList(versionCounter, false).isEmpty()))
				return null;
			
			//accumulate past version updates
			List<SharedDataUpdate> list = new ArrayList<>();
			for (int i = Math.max(currentVersion, 0) ; i < versionCounter ; i++) {
				for (SharedDataUpdate update : getUpdateList(i, false)) {
					update.setSent();
					list.add(update);
				}
			}
			
			//add current version updates and increment version
			synchronized (updates) {
				for (SharedDataUpdate update : getUpdateList(versionCounter, false)) {
					update.setSent();
					list.add(update);
				}
				if (list.isEmpty())
					return null;
				connections.put(connection, (Integer)versionCounter);
				versionCounter++;
				ans = new ContinuousDataContainer(dataIdentifier, versionCounter, list);
				
				if (disposeDistributed) {
					disposeDistributed();
				}
				
			}
		}
		
		
		return ans;
	}
		
	@SuppressWarnings("unchecked")
	private List<SharedDataUpdate> getUpdateList(Integer versionCounter, boolean insertIfNull) {
		if (!insertIfNull)
			return updates.getOrDefault(versionCounter, Collections.EMPTY_LIST);
		else {
			List<SharedDataUpdate> list = updates.get(versionCounter);
			if (list == null) {
				list = new ArrayList<>();
				updates.put(versionCounter, list);
			}
			return list;
		}
	}

	public void update(SharedDataUpdate update) {
		synchronized (updates) {
			List<SharedDataUpdate> list = getUpdateList(versionCounter, true);
			list.add(update);
		}
	}

	/**
	 * dispose data that is already distributed to all registered clients
	 */
	private void disposeDistributed() {
		if (!disposeDistributed)
			return;
		int lowestDistributedVersion = versionCounter;
		for (Entry<Connection<?>, Integer> e : connections.entrySet()) {
			int version = e.getValue();
			if (version < lowestDistributedVersion)
//				lowestDistributedVersion = version-1;
				lowestDistributedVersion = version;
		}
		
		for (int i = disposedCounter ; i <= lowestDistributedVersion ; i++) {
			updates.remove((Integer)i);
		}
		
		disposedCounter = lowestDistributedVersion;
	}

	public boolean isDisposeDistributed() {
		return disposeDistributed;
	}

	public void setDisposeDistributed(boolean disposeDistributed) {
		this.disposeDistributed = disposeDistributed;
	}
	
	@Override
	public void connectionClosed(Connection<?> connection) {
		synchronized (updates) {
			super.connectionClosed(connection);
			disposeDistributed();
		}
	}

	@Override
	public synchronized int getVersion() {
		return versionCounter;
	}
	
	@Override
	public boolean isEmpty() {
		return updates.isEmpty();
	}


}
