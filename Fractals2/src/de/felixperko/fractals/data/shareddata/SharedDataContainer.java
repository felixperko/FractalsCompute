package de.felixperko.fractals.data.shareddata;

import java.io.Serializable;
import java.util.List;

public class SharedDataContainer implements Serializable{
	
	private static final long serialVersionUID = 4425724711433635913L;
	
	List<SharedDataUpdate> updates;
	int version;
	
	public SharedDataContainer(int version, List<SharedDataUpdate> updates) {
		this.version = version;
		this.updates = updates;
	}
}
