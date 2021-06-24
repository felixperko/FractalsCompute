package de.felixperko.fractals.system.parameters.attributes;

/*
 * interface for accessing attributes of parameters.
 */
public interface ParamAttribute<T> {
	
	String getName();
	String getQualifiedName();
	
	Class<?> getAttributeClass();
	
	T getValue();
	void applyValue(Object value);
}
