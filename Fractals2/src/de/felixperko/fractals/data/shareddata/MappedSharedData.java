package de.felixperko.fractals.data.shareddata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.infra.connection.ConnectionListener;

/**
 * Shares the current state between clients
 */
public class MappedSharedData<T> extends SharedData<MappedSharedDataUpdate<T>> {
	
	int versionCounter = 0;
	Map<String, T> map = new HashMap<>();
	Map<String, Long> updateTimes = new HashMap<>();
	Map<Connection<?>, Set<String>> pendingConnectionUpdates = new HashMap<>();
	boolean disposeDistributed;
	
	public MappedSharedData(String dataIdentifier, boolean disposeDistributed) {
		super(dataIdentifier);
		this.disposeDistributed = disposeDistributed;
	}
	
	@Override
	public synchronized DataContainer getUpdates(Connection<?> connection) {
		
		Set<String> pendingUpdates = pendingConnectionUpdates.get(connection);
		MappedSharedDataUpdate<T> update = null;
		if (pendingUpdates == null) {
			if (!map.isEmpty()) {
				update = new MappedSharedDataUpdate<>();
				for (Entry<String, T> e : map.entrySet())
					update.setValue(e.getKey(), e.getValue());
			}
			
			pendingConnectionUpdates.put(connection, new HashSet<>());
			connection.addConnectionListener(new ConnectionListener() {
				@Override
				public void connectionClosed(Connection<?> connection) {
					Set<String> keySet = pendingConnectionUpdates.remove(connection);
					if (disposeDistributed) {
						Set<Connection<?>> otherConnections = null;
						if (pendingConnectionUpdates.keySet().size() > 1) {
							otherConnections = new HashSet<>(pendingConnectionUpdates.keySet());
							otherConnections.remove(connection);
						}
						if (otherConnections != null) {
							for (String key : keySet) {
								boolean lastDistribution = true;
								for (Connection<?> conn : otherConnections) {
									if (pendingConnectionUpdates.get(conn).contains(key)){
										lastDistribution = false;
										break;
									}
								}
								if (lastDistribution) {
									map.remove(key);
									updateTimes.remove(key);
								}
							}
						} else {
							map.clear();
							updateTimes.clear();
						}
					}
				}
			});
			
		} else {
			if (!pendingUpdates.isEmpty()) {
				update = new MappedSharedDataUpdate<>();
				Set<Connection<?>> otherConnections = null;
				if (disposeDistributed && pendingConnectionUpdates.keySet().size() > 1) {
					otherConnections = new HashSet<>(pendingConnectionUpdates.keySet());
					otherConnections.remove(connection);
				}
				for (String key : pendingUpdates) {
					update.setValue(key, map.get(key));
					if (disposeDistributed) {
						boolean lastDistribution = true;
						if (otherConnections != null) {
							for (Connection<?> conn : otherConnections) {
								if (pendingConnectionUpdates.get(conn).contains(key)) {
									lastDistribution = false;
									break;
								}
							}
							if (lastDistribution) {
								map.remove(key);
								updateTimes.remove(key);
							}
						}
						
					}
						
				}
				pendingUpdates.clear();
			}
		}
		
		if (update != null)
			return new DataContainer(dataIdentifier, update);
		else 
			return null;
	}

	@Override
	public synchronized void update(MappedSharedDataUpdate<T> update) {
		boolean changed = false;
		if (update.isClearExisting()) {
			for (String key : map.keySet()) {
				if (setValue(key, null))
					changed = true;
			}
		}
		for (Entry<String, T> e : update.getUpdateMap().entrySet()) {
			if (setValue(e.getKey(), e.getValue()))
				changed = true;
		}
		if (changed)
			versionCounter++;
	}

	private boolean setValue(String key, T value) {
		Long thisTime = System.nanoTime();
		Long lastTime = updateTimes.get(key);
		if (lastTime == null || lastTime < thisTime) {
			map.put(key, value);
			updateTimes.put(key, thisTime);
			for (Connection<?> conn : pendingConnectionUpdates.keySet())
				pendingConnectionUpdates.get(conn).add(key);
			return true;
		}
		return false;
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
		return map.isEmpty();
	}

}
