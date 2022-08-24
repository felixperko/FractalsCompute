package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParamValueType implements Serializable{
	
	private static final long serialVersionUID = -1932439926056557969L;
	
	ParamConfiguration configuration;
	String uid;
	String name;
	List<ParamValueField> fields = null;
	
	public ParamValueType(String uid, String name, ParamValueField... fields) {
		this.uid = uid;
		this.name = name;
		this.fields = new ArrayList<>();
		if (fields != null) {
			for (int i = 0 ; i < fields.length ; i++) {
				this.fields.add(fields[i]);
			}
		}
	}

	public ParamConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ParamConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public String getUID() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ParamValueField> getSubTypes() {
		return fields;
	}

	public void setSubTypes(List<ParamValueField> subTypes) {
		this.fields = subTypes;
	}
}
