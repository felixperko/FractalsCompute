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

	
	@Override
	public boolean evaluateChanged(ParamSupplier old) {
		if (old == null) {
			return false;
		} else if (!(old instanceof StaticParamSupplier)) {
			return true;
		} else {
			return !((StaticParamSupplier)old).obj.equals(obj);
		}
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		if (!(arg0 instanceof StaticParamSupplier))
			return false;
		StaticParamSupplier other = (StaticParamSupplier)arg0;
		if (this.obj == null)
			return other.obj == null;
		if (other.obj == null) //this.obj can't be null
			return false;
		return this.obj.equals(other.obj);
	}

}
