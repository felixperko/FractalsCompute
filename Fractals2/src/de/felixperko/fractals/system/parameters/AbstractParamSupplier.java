package de.felixperko.fractals.system.parameters;

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
	public <C> C getGeneral(Class<C> cls) {
		Object obj = get(0,0);
		return (C) obj;
	}
}
