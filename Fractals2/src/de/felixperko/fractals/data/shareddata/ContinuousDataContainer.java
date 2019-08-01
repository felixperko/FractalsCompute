package de.felixperko.fractals.data.shareddata;

import java.util.List;

public class ContinuousDataContainer extends DataContainer{
	
	private static final long serialVersionUID = 4425724711433635913L;
	
	int version;
	
	public ContinuousDataContainer(String dataIdentifier, int version, List<SharedDataUpdate<?>> updates) {
		super(dataIdentifier, updates);
		this.version = version;
	}
}
