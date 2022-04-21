package de.felixperko.fractals.system.parameters.attributes;

public abstract class AbstractParamAttribute<T> implements ParamAttribute<T> {
	
	String name;
	String prefix;
	
	public AbstractParamAttribute(String prefix, String name) {
		this.prefix = prefix;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getQualifiedName() {
		return prefix+"."+name;
	}
	
	@Override
	public T getMinValue() {
		return null;
	}
	
	@Override
	public T getMaxValue() {
		return null;
	}
	
	@Override
	public void setRange(Object newMin, Object newMax) {
	}
}
