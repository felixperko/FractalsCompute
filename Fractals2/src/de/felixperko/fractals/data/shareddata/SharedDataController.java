package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.List;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.messages.SharedDataUpdateMessage;

public class SharedDataController {
	
	List<SharedData<?>> data;
	
	public void addSharedData(SharedData<?> sharedData) {
		data.add(sharedData);
	}
	
	public void removeSharedData(SharedData<?> sharedData) {
		data.remove(sharedData);
	}
	
	public List<DataContainer> getUpdates(Connection<?> connection) {
		
		List<DataContainer> list = new ArrayList<>();
		
		for (SharedData<?> sharedData : data) {
			if (sharedData.hasUpdate(connection)) {
				DataContainer container = sharedData.getUpdates(connection);
				if (container != null) {
					list.add(container);
				}
			}
		}
		
		return list;
	}
	
	public void sendMessageIfUpdatesAvailable(Connection<?> connection) {
		if (data.isEmpty())
			return;
		List<DataContainer> list = getUpdates(connection);
		if (!list.isEmpty()) {
			connection.writeMessage(new SharedDataUpdateMessage(list));
		}
	}
}
