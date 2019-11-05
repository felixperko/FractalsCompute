package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.systems.infra.Selection;

public class ParameterConfiguration implements Serializable{
	
	private static final long serialVersionUID = -6181978740061515601L;
	
	List<ParameterDefinition> parameters = new ArrayList<>();
	Map<String, List<ParameterDefinition>> calculatorParameters = new HashMap<>();
	Map<String, ParamValueType> types = new HashMap<>();
	Map<String, Selection<?>> selections = new HashMap<>();
	Map<String, List<ParamValueType>> listTypes = new HashMap<>();
	
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
	
	public List<ParameterDefinition> getCalculatorParameters(String calculatorName){
		List<ParameterDefinition> list = this.calculatorParameters.get(calculatorName);
		return list;
	}
	
	public void addCalculatorParameters(String calculatorName, List<ParameterDefinition> parameterDefinitions) {
		this.calculatorParameters.put(calculatorName, parameterDefinitions);	
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

	public void addListTypes(String listId, ParamValueType... possibleTypes) {
		listTypes.put(listId, Arrays.asList(possibleTypes));
	}
	
	public List<ParamValueType> getListTypes(String listId) {
		List<ParamValueType> ans = listTypes.get(listId);
//		if (ans == null)
//			ans = new ArrayList<>();
		return ans;
	}
}
