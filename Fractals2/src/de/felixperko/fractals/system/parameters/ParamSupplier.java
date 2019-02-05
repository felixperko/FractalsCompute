package de.felixperko.fractals.system.parameters;

import java.io.Serializable;

public interface ParamSupplier extends Serializable{
	public String getName();
	public Object get(int pixel, int sample);
	public ParamSupplier copy();
	public boolean isResetCalculation();
	public void setResetCalculation();
}
