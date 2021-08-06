package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ParamDefinition implements Serializable{
	
	private static final long serialVersionUID = 7667944768022310166L;

	ParamConfiguration configuration;
	
	String name;
	String description;
	List<String> hints = new ArrayList<>();
	
	String category;
	
	List<Class<? extends ParamSupplier>> possibleClasses;
	List<ParamValueType> possibleValueTypes;
	
	boolean resetRendererOnChange = true;
	boolean visible = true;
	
	Object defaultValue = null;
	
	public ParamDefinition(String name, String category, Class<? extends ParamSupplier> cls, ParamValueType... possibleValueTypes) {
		this.name = name;
		this.category = category;
		this.possibleClasses = new ArrayList<>();
		this.possibleValueTypes = Arrays.asList(possibleValueTypes);
		this.possibleClasses.add(cls);
	}
	
	public ParamDefinition(String name, String category, List<Class<? extends ParamSupplier>> classes, ParamValueType... possibleValueTypes) {
		this.name = name;
		this.category = category;
		this.possibleClasses = classes;
		this.possibleValueTypes = Arrays.asList(possibleValueTypes);
	}
	
	public ParamDefinition(String name, String category, ParamValueType possibleValue, Class<? extends ParamSupplier>... classes) {
		this.name = name;
		this.category = category;
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

	public ParamConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ParamConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ParamDefinition withDescription(String description) {
		this.description = description;
		return this;
	}
	
	public ParamDefinition withHints(String... hints) {
		for (String hint : hints)
			this.hints.add(hint);
		return this;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public String getCategory() {
		return category;
	}
	
	public List<String> getHints() {
		return hints;
	}
	
	public String getHintValue(String hintName, boolean returnEmptyStringIfAbsent){
		for (String hint : hints)
			if (hint.startsWith(hintName))
				return hint.split(" ", 2)[1];
		if (returnEmptyStringIfAbsent)
			return "";
		else
			return null;
	}
	
	public String getHintAttributeValue(String hintName, String attributeName){
		for (String attribute : getHintValue(hintName, true).split(" ")){
            String[] pair = attribute.split("=");
            if (pair.length != 2)
                continue;
            String key = pair[0];
            String value = pair[1];
            if (key.equals(attributeName))
            	return value;
		}
		return null;
	}
	
	public Double getHintAttributeDoubleValue(String hintName, String attributeName){
		String hintAttribute = getHintAttributeValue(hintName, attributeName);
		if (hintAttribute == null)
			return null;
		return Double.parseDouble(hintAttribute);
	}
	
	public boolean isResetRendererOnChange(){
		return resetRendererOnChange;
	}
	
	public void setResetRendererOnChange(boolean resetOnChange){
		this.resetRendererOnChange = resetOnChange;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public ParamDefinition withVisible(boolean visible) {
		setVisible(visible);
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((hints == null) ? 0 : hints.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((possibleClasses == null) ? 0 : possibleClasses.hashCode());
		result = prime * result + ((possibleValueTypes == null) ? 0 : possibleValueTypes.hashCode());
		result = prime * result + (resetRendererOnChange ? 1231 : 1237);
		result = prime * result + (visible ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParamDefinition other = (ParamDefinition) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (hints == null) {
			if (other.hints != null)
				return false;
		} else if (!hints.equals(other.hints))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (possibleClasses == null) {
			if (other.possibleClasses != null)
				return false;
		} else if (!possibleClasses.equals(other.possibleClasses))
			return false;
		if (possibleValueTypes == null) {
			if (other.possibleValueTypes != null)
				return false;
		} else if (!possibleValueTypes.equals(other.possibleValueTypes))
			return false;
		if (resetRendererOnChange != other.resetRendererOnChange)
			return false;
		if (visible != other.visible)
			return false;
		return true;
	}
}
