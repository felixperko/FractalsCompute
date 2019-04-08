package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ParameterDefinition implements Serializable{
	
	private static final long serialVersionUID = 7667944768022310166L;

	ParameterConfiguration configuration;
	
	String name;
	String description;
	
	List<Class<? extends ParamSupplier>> possibleClasses;
	List<ParamValueType> possibleValueTypes;
	
	Object defaultValue = null;
	
	public ParameterDefinition(String name, Class<? extends ParamSupplier> cls, ParamValueType... possibleValueTypes) {
		this.name = name;
		this.possibleClasses = new ArrayList<>();
		this.possibleValueTypes = new ArrayList<>();
		this.possibleClasses.add(cls);
	}
	
	public ParameterDefinition(String name, List<Class<? extends ParamSupplier>> classes, ParamValueType... possibleValueTypes) {
		this.name = name;
		this.possibleClasses = classes;
		this.possibleValueTypes = Arrays.asList(possibleValueTypes);
	}
	
	public ParameterDefinition(String name, ParamValueType possibleValue, Class<? extends ParamSupplier>... classes) {
		this.name = name;
		this.possibleClasses = Arrays.asList(classes);
		this.possibleValueTypes = new ArrayList<>();
		this.possibleValueTypes.add(possibleValue);
	}

	public List<Class<? extends ParamSupplier>> getPossibleClasses() {
		return possibleClasses;
	}

	public List<ParamValueType> getPossibleValueTypes() {
		return possibleValueTypes;
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
	
	public String getDescription() {
		return description;
	}
	
	public ParameterDefinition withDescription(String description) {
		this.description = description;
		return this;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
