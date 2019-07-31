package de.felixperko.fractals.data.shareddata;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MappedSharedDataUpdate<T> implements SharedDataUpdate<T>, Serializable {

	private static final long serialVersionUID = -510929972128464350L;
	
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
	
	public Map<String, T> getUpdateMap(){
		return updates;
	}
	
	public Collection<T> getUpdates(){
		return updates.values();
	}
	
	public boolean isClearExisting() {
		return clearExisting;
	}
	
	public void setClearExisting() {
		clearExisting = true;
	}
}
