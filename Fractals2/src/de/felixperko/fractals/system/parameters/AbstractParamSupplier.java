package de.felixperko.fractals.system.parameters;

public abstract class AbstractParamSupplier implements ParamSupplier {
	
	private static final long serialVersionUID = -7127742325514423406L;
	
	String name;
	
	boolean systemRelevant = false;
	boolean layerRelevant = false;
	boolean viewRelevant = false;
	
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

	public void setSystemRelevant(boolean systemRelevant) {
		this.systemRelevant = systemRelevant;
	}

	public boolean isLayerRelevant() {
		return layerRelevant;
	}

	public void setLayerRelevant(boolean layerRelevant) {
		this.layerRelevant = layerRelevant;
	}

	public boolean isViewRelevant() {
		return viewRelevant;
	}

	public void setViewRelevant(boolean viewRelevant) {
		this.viewRelevant = viewRelevant;
	}
}
