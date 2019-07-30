package de.felixperko.fractals.data.shareddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;

public class MappedSharedDataUpdate<T> implements SharedDataUpdate {

	boolean sent = false;
	Map<String, T> updates = new HashMap<>();
	boolean clearExisting = false;
	
	public MappedSharedDataUpdate() {
		
	}
	
	public MappedSharedDataUpdate(String key, T value) {
		setValue(key, value);
	}
	
	@Override
	public boolean isSent() {
		return sent;
	}

	@Override
	public void setSent() {
		this.sent = true;
	}
	
	public void setValue(String key, T value) {
		updates.put(key, value);
	}
	
	public void removeValue(String key) {
		updates.put(key, null);
	}
	
	public Map<String, T> getUpdates(){
		return updates;
	}
	
	public boolean isClearExisting() {
		return clearExisting;
	}
	
	public void setClearExisting() {
		clearExisting = true;
	}
}
