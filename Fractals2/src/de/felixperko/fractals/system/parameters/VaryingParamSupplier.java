package de.felixperko.fractals.system.parameters;

public abstract class VaryingParamSupplier implements ParamSupplier{
	
	String name;
	
	public VaryingParamSupplier(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
}
