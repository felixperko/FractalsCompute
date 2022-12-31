package de.felixperko.fractals.system.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ParamDefinition implements Serializable{
	
	private static final long serialVersionUID = 7667944768022310166L;

	ParamConfiguration configuration;
	
	String uid;
	String displayName;
	String description;
	List<String> hints = new ArrayList<>();
	
	String category;
	
	List<Class<? extends ParamSupplier>> possibleClasses;
	ParamValueType valueType;
	double valueTypeVersion;
	
	boolean resetRendererOnChange = true;
	boolean visible = true;
	
	Object defaultValue = null;
	
	public ParamDefinition(String uid, String displayName, String category, Class<? extends ParamSupplier> cls, ParamValueType valueType, double valueTypeVersion) {
		this.uid = uid;
		this.displayName = displayName;
		this.category = category;
		this.possibleClasses = new ArrayList<>();
		this.valueType = valueType;
		this.valueTypeVersion = valueTypeVersion;
		this.possibleClasses.add(cls);
	}
	
	public ParamDefinition(String uid, String displayName, String category, List<Class<? extends ParamSupplier>> classes, ParamValueType valueType, double valueTypeVersion) {
		this.uid = uid;
		this.displayName = displayName;
		this.category = category;
		this.possibleClasses = classes;
		this.valueType = valueType;
		this.valueTypeVersion = valueTypeVersion;
	}
	
	public ParamDefinition(String uid, String displayName, String category, ParamValueType valueType, double valueTypeVersion, Class<? extends ParamSupplier>... classes) {
		this.uid = uid;
		this.displayName = displayName;
		this.category = category;
		this.possibleClasses = Arrays.asList(classes);
		this.valueType = valueType;
		this.valueTypeVersion = valueTypeVersion;
	}

	public List<Class<? extends ParamSupplier>> getPossibleClasses() {
		return possibleClasses;
	}

	public ParamValueType getValueType() {
		return valueType;
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
		return displayName;
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

//	public Object getDefaultValue() {
//		return defaultValue;
//	}
	
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
	
	public ParamDefinition setResetRendererOnChange(boolean resetOnChange){
		this.resetRendererOnChange = resetOnChange;
		return this;
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
		return Objects.hash(uid);
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
		return Objects.equals(uid, other.uid);
	}

	public double getValueTypeVersion() {
		return valueTypeVersion;
	}

	public void setValueTypeVersion(double valueTypeVersion) {
		this.valueTypeVersion = valueTypeVersion;
	}
}
