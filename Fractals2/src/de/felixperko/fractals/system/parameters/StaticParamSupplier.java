package de.felixperko.fractals.system.parameters;

public class StaticParamSupplier extends AbstractParamSupplier {
	
	Object obj;
	
	public StaticParamSupplier(String name, Object obj) {
		super(name);
		this.obj = obj;
	}

	@Override
	public Object get(int pixel, int sample) {
		return obj;
	}

	@Override
	public ParamSupplier copy() {
		return new StaticParamSupplier(name, obj);
	}
}
