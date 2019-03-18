package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParamValueType implements Serializable{
	
	private static final long serialVersionUID = -1932439926056557969L;
	
	ParameterConfiguration configuration;
	String name;
	List<ParamValueField> fields = null;
	
	public ParamValueType(String name, ParamValueField... fields) {
		this.name = name;
		if (fields != null && fields.length > 0) {
			this.fields = new ArrayList<>();
			for (int i = 0; i < fields.length; i++) {
				this.fields.add(fields[i]);
			}
		}
	}

	public ParameterConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ParameterConfiguration configuration) {
		this.configuration = configuration;
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
