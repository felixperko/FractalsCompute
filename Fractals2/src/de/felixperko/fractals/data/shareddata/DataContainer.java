package de.felixperko.fractals.data.shareddata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataContainer implements Serializable{

	private static final long serialVersionUID = 8570824204254426488L;
	
	String identifier;
	protected List<SharedDataUpdate> updates;

	public DataContainer(String dataIdentifier, List<SharedDataUpdate> updates) {
		this.identifier = dataIdentifier;
		this.updates = updates;
	}
	
	public DataContainer(String dataIdentifier, MappedSharedDataUpdate<?> update) {
		this.identifier = dataIdentifier;
		this.updates = new ArrayList<>();
		this.updates.add(update);
	}

	public List<SharedDataUpdate> getUpdates(){
		return updates;
	}

	public String getIdentifier() {
		return identifier;
	}
}