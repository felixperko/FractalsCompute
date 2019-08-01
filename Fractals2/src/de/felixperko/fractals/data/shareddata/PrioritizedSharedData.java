package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import de.felixperko.fractals.network.Connection;

public class PrioritizedSharedData extends SharedData<PrioritizedSharedDataUpdate>{
	
	Map<Connection<?>, PriorityQueue<PrioritizedSharedDataUpdate>> updates = new HashMap<>();
	
	int versionCounter = 0;
	
	int copyCount = 1;
	
	public PrioritizedSharedData(String dataIdentifier) {
		super(dataIdentifier);
	}

	@Override
	public DataContainer getUpdates(Connection<?> connection) {
		PrioritizedSharedDataUpdate update = null;

		List<SharedDataUpdate<?>> list = new ArrayList<>();
		synchronized (updates){
			for (int i = 0 ; i < copyCount ; i++) {
				update = getQueue(connection).poll();
				if (update == null) {
					if (list.isEmpty())
						return null;
					else
						break;
				}
				list.add(update);
			}
		}
		
		DataContainer container = new DataContainer(dataIdentifier, list);
		return container;
		
	}

	public int getCopyCount() {
		return copyCount;
	}

	public void setCopyCount(int copyCount) {
		this.copyCount = copyCount;
	}

	private PriorityQueue<PrioritizedSharedDataUpdate> getQueue(Connection<?> connection) {
		PriorityQueue<PrioritizedSharedDataUpdate> queue = updates.get(connection);
		if (queue == null){
			connections.put(connection, 0);
			queue = new PriorityQueue<PrioritizedSharedDataUpdate>();
			updates.put(connection, queue);
		}
		return queue;
	}

	@Override
	public void update(PrioritizedSharedDataUpdate update) {
		synchronized (updates) {
			versionCounter++;
			for (Connection<?> connection : connections.keySet())
				getQueue(connection).add(update);
		}
	}
	
	@Override
	public void connectionClosed(Connection<?> connection) {
		synchronized (updates) {
			super.connectionClosed(connection);
			updates.remove(connection);
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
	
	public void clearSharedData(Connection<?> connection) {
		if (connections.containsKey(connection))
			updates.get(connection).clear();
	}
	
	public void clearSharedData() {
		updates.clear();
	}
}
