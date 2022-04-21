package de.felixperko.fractals.util.serialization.jackson;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonAbstractTypedObject implements JsonTypedObject, Serializable {
	
	private static final long serialVersionUID = -8053650093006124773L;
	
	String type;
	
	public JsonAbstractTypedObject(String type) {
		this.type = type;
	}
	
	JsonAbstractTypedObject() {
	}
	
	void setType(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}
}
