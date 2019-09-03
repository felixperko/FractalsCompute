package de.felixperko.fractals.system.parameters.suppliers;

import java.io.Serializable;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public interface ParamSupplier extends Serializable{
	
	public String getName();
	
	public Object get(ComplexNumber chunkPos, int pixel, int sample, SystemContext systemContext);
	
	public boolean isSystemRelevant();
	public boolean isLayerRelevant();
	public boolean isViewRelevant();
	
	public AbstractParamSupplier setSystemRelevant(boolean relevant);
	public AbstractParamSupplier setLayerRelevant(boolean relevant);
	public AbstractParamSupplier setViewRelevant(boolean relevant);
	
	public boolean isChanged();

	public ParamSupplier copy();

	public <C> C getGeneral(Class<C> cls);

	public void updateChanged(ParamSupplier old);
	public boolean evaluateChanged(ParamSupplier old);
}
