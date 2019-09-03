package de.felixperko.fractals.system.parameters.suppliers;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public abstract class AbstractParamSupplier implements ParamSupplier {
	
	private static final long serialVersionUID = -7127742325514423406L;
	
	String name;
	
	boolean systemRelevant = false;
	boolean layerRelevant = false;
	boolean viewRelevant = false;
	
	protected boolean changed = false;
	
	public AbstractParamSupplier(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public boolean isSystemRelevant() {
		return systemRelevant;
	}

	public AbstractParamSupplier setSystemRelevant(boolean systemRelevant) {
		this.systemRelevant = systemRelevant;
		return this;
	}

	public boolean isLayerRelevant() {
		return layerRelevant;
	}

	public AbstractParamSupplier setLayerRelevant(boolean layerRelevant) {
		this.layerRelevant = layerRelevant;
		return this;
	}

	public boolean isViewRelevant() {
		return viewRelevant;
	}

	public AbstractParamSupplier setViewRelevant(boolean viewRelevant) {
		this.viewRelevant = viewRelevant;
		return this;
	}
	
	@Override
	public void updateChanged(ParamSupplier old) {
		changed = evaluateChanged(old);
	}
	
	public abstract boolean evaluateChanged(ParamSupplier old);
	
	/**
	 * updateChanged() has to be called first
	 */
	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public Object getGeneral() {
		return get(null, null, 0, 0);
	}
	
	@Override
	public <C> C getGeneral(Class<C> cls) {
		return cast(getGeneral(), cls);
	}
	
	@Override
	public Object get(SystemContext systemContext, ComplexNumber chunkPos, int pixel, int sample) {
		return get(systemContext, chunkPos, pixel, sample);
	}
	
	@Override
	public <C> C get(SystemContext systemContext, Class<C> valueCls, ComplexNumber chunkPos, int pixel, int sample) {
		return cast(get(systemContext, chunkPos, pixel, sample), valueCls);
	}
	
	@SuppressWarnings("unchecked")
	private <C> C cast(Object object, Class<C> cls) {
		if (cls.isInstance(object))
			return (C)object;
		throw new IllegalArgumentException("object can't be cast to class");
	}
}
