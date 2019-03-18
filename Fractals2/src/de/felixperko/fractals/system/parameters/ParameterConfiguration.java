package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.Selection;

public class ParameterConfiguration implements Serializable{
	
	private static final long serialVersionUID = -6181978740061515601L;
	
	List<ParameterDefinition> parameters = new ArrayList<>();
	Map<String, ParamValueType> types = new HashMap<>();
	Map<String, Selection<?>> selections = new HashMap<>();
	
	public void addParameterDefinition(ParameterDefinition definition) {
		parameters.add(definition);
		definition.setConfiguration(this);
	}
	
	public void addValueType(ParamValueType valueType) {
		types.put(valueType.getName(), valueType);
		valueType.setConfiguration(this);
	}
	
	public ParamValueType getType(String name) {
		return types.get(name);
	}

	public List<ParameterDefinition> getParameters() {
		return parameters;
	}

	public Map<String, ParamValueType> getTypes() {
		return types;
	}

	public void addParameterDefinitions(List<ParameterDefinition> defs) {
		for (ParameterDefinition def : defs) {
			addParameterDefinition(def);
		}
	}

	public void addValueTypes(ParamValueType[] types) {
		for (ParamValueType type : types) {
			addValueType(type);
		}
	}
	
	public Selection<?> getSelection(String name){
		return selections.get(name);
	}
	
	public void addSelection(Selection<?> selection) {
		selections.put(selection.getName(), selection);
	}
}
