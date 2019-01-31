package de.felixperko.fractals.system.parameters;

public interface ParamSupplier {
	public String getName();
	public Object get(int pixel);
}
