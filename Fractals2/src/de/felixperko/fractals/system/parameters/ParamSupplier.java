package de.felixperko.fractals.system.parameters;

import java.io.Serializable;

public interface ParamSupplier extends Serializable{
	
	public String getName();
	
	public Object get(int pixel, int sample);
	
	public boolean isSystemRelevant();
	public boolean isLayerRelevant();
	public boolean isViewRelevant();
	
	public void setSystemRelevant(boolean relevant);
	public void setLayerRelevant(boolean relevant);
	public void setViewRelevant(boolean relevant);

	public ParamSupplier copy();
}
