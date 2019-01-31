package de.felixperko.fractals.system.parameters;

public class StaticParamSupplier implements ParamSupplier {
	
	String name;
	Object obj;
	
	public StaticParamSupplier(String name, Object obj) {
		this.name = name;
		this.obj = obj;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object get(int pixel) {
		return obj;
	}
}
