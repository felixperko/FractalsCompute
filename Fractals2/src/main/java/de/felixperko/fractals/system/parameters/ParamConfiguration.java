package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.Selection;

public class ParamConfiguration implements Serializable{
	
	private static final long serialVersionUID = -6181978740061515601L;
	
	String uid;
	double version;
	
	List<ParamDefinition> parameters = new ArrayList<>();
	Map<String, ParamDefinition> definitionsByName = new HashMap<>();
	Map<String, ParamDefinition> definitionsByUID = new HashMap<>();
	Map<String, ParamSupplier> defaultValues = new HashMap<>();
	Map<String, List<ParamDefinition>> calculatorParameters = new HashMap<>();
	Map<String, Map<String, ParamSupplier>> calculatorDefaultValues = new HashMap<>();
	Map<String, ParamValueType> types = new HashMap<>();
	Map<String, Selection<?>> selections = new HashMap<>();
	Map<String, List<ParamValueType>> listTypes = new HashMap<>();
	
	public ParamConfiguration(String uid, double version) {
		this.uid = uid;
		this.version = version;
	}
	
	public void addParameterDefinition(ParamDefinition definition, ParamSupplier defaultValue) {
		parameters.add(definition);
		definitionsByName.put(definition.getName(), definition);
		definitionsByUID.put(definition.getUID(), definition);
		definition.setConfiguration(this);
		if (defaultValue != null)
			addDefaultValue(defaultValue);
	}

	public void addDefaultValue(ParamSupplier defaultValue) {
		defaultValues.put(defaultValue.getUID(), defaultValue);
	}
	
	public void addValueType(ParamValueType valueType) {
		types.put(valueType.getName(), valueType);
		valueType.setConfiguration(this);
	}
	
	public ParamValueType getType(String name) {
		return types.get(name);
	}

	public List<ParamDefinition> getParameters() {
		return parameters;
	}
	
	public List<ParamDefinition> getCalculatorParameters(String calculatorName){
		List<ParamDefinition> list = this.calculatorParameters.get(calculatorName);
		return list;
	}
	
	public void addCalculatorParameters(String calculatorName, List<ParamDefinition> parameterDefinitions, List<ParamSupplier> defaultValues) {
		this.calculatorParameters.put(calculatorName, parameterDefinitions);
		if (defaultValues != null){
			Map<String, ParamSupplier> calculatorDefaultMap = new HashMap<>();
			for (ParamSupplier supp : defaultValues)
				calculatorDefaultMap.put(supp.getUID(), supp);
			this.calculatorDefaultValues.put(calculatorName, calculatorDefaultMap);
		}
		for (ParamDefinition definition : parameterDefinitions)
			definition.setConfiguration(this);
	}
	
	public Map<String, ParamValueType> getTypes() {
		return types;
	}

	public void addParameterDefinitions(List<ParamDefinition> defs) {
		defs.forEach(def -> addParameterDefinition(def, null));
	}
	
	public void addDefaultValues(List<ParamSupplier> defaultValues) {
		defaultValues.forEach(val -> addDefaultValue(val));
	}
	
	public void addParameterDefinitions(List<ParamDefinition> defs, List<ParamSupplier> defaultValues) {
		addParameterDefinitions(defs);
		addDefaultValues(defaultValues);
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
	
	public Map<String, Selection<?>> getSelections(){
		return selections;
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

	public Map<String, Map<String, ParamSupplier>> getCalculatorDefaultValues() {
		return calculatorDefaultValues;
	}

	public ParamSupplier getDefaultValue(String calculator, String uid) {
		ParamSupplier supp = defaultValues.get(uid);
		if (supp != null || calculator == null)
			return supp;
		Map<String, ParamSupplier> map = calculatorDefaultValues.get(calculator);
		return map == null ? null : map.get(uid);
	}
	
	public ParamDefinition getParamDefinitionByName(String name){
		return definitionsByName.get(name);
	}
	
	public ParamDefinition getParamDefinitionByUID(String uid){
		return definitionsByUID.get(uid);
	}
	
	public Map<String, String> getUIDsByName(){
        Map<String, String> uidsByName = new HashMap<>();
        for (ParamDefinition def : parameters){
            uidsByName.put(def.getName(), def.getUID());
        }
        return uidsByName;
	}

	public ParamDefinition getParamDefinition(ParamSupplier supp) {
		return getParamDefinitionByUID(supp.getUID());
	}
	
	public String getName(String uid) {
		ParamDefinition def = getParamDefinitionByUID(uid);
		return def != null ? def.getName() : null;
	}
	
	public String getName(ParamSupplier supp) {
		return getName(supp.getUID());
	}

	public String getUid() {
		return uid;
	}

	public double getVersion() {
		return version;
	}
}
