package de.felixperko.fractals.data.shareddata;

import java.io.Serializable;
import java.util.List;

public class DataContainer implements Serializable{

	private static final long serialVersionUID = 8570824204254426488L;
	
	protected List<SharedDataUpdate> updates;

	public DataContainer(List<SharedDataUpdate> updates) {
		this.updates = updates;
	}
	
	public List<SharedDataUpdate> getUpdates(){
		return updates;
	}
}