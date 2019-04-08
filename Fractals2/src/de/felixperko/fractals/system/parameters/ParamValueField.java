package de.felixperko.fractals.system.parameters;

import java.io.Serializable;

public class ParamValueField implements Serializable{

	private static final long serialVersionUID = -3757757582361274134L;
	
	String name;
	String description;
	
	ParamValueType type;
	Object defaultValue;
	ParamValueType subType = null;
	
	public ParamValueField(String name, ParamValueType type) {
		this.name = name;
		this.type = type;
	}
	
	public ParamValueField(String name, ParamValueType type, Object defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ParamValueField withDescription(String description) {
		this.description = description;
		return this;
	}

	public ParamValueType getType() {
		return type;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public ParamValueField setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public ParamValueField setSubType(ParamValueType subType) {
		this.subType = subType;
		return this;
	}
}
