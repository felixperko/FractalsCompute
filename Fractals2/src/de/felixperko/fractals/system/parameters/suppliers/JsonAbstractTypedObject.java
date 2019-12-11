package de.felixperko.fractals.system.parameters.suppliers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonAbstractTypedObject implements JsonTypedObject {
	
	String type;
	
	public JsonAbstractTypedObject(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}
}
