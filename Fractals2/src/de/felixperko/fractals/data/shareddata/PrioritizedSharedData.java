package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import de.felixperko.fractals.network.Connection;

public class PrioritizedSharedData extends SharedData<PrioritizedSharedDataUpdate>{
	
	Map<Connection<?>, PriorityQueue<PrioritizedSharedDataUpdate>> updates = new HashMap<>();

	@Override
	public DataContainer getUpdates(Connection<?> connection) {
		PrioritizedSharedDataUpdate update = null;
		synchronized (updates){
			update = getQueue(connection).poll();
			if (update == null)
				return null;
		}
		
		List<SharedDataUpdate> list = new ArrayList<>();
		list.add(update);
		DataContainer container = new DataContainer(list);
		return container;
		
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
	
	
}
