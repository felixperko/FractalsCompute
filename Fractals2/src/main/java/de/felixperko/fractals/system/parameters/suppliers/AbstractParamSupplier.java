package de.felixperko.fractals.system.parameters.suppliers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;

@JsonPropertyOrder({"type", "name", "attr"})
public abstract class AbstractParamSupplier implements ParamSupplier {

	static final int ATTR_CHANGED = 1 << 0;
	static final int ATTR_SYSTEM_RELEVANT = 1 << 2;
	static final int ATTR_LAYER_RELEVANT = 1 << 3;
	static final int ATTR_VIEW_RELEVANT = 1 << 4;
	
	private static final long serialVersionUID = -7127742325514423406L;
	
	protected String uid;
	
	@JsonIgnore
	boolean systemRelevant = false;
	@JsonIgnore
	boolean layerRelevant = false;
	@JsonIgnore
	boolean viewRelevant = false;

	@JsonIgnore
	protected boolean changed = false;
	
	public AbstractParamSupplier(String uid) {
		this.uid = uid;
	}
	
	public boolean isViewRelevant() {
		return viewRelevant;
	}

	public void setViewRelevant(boolean viewRelevant) {
		this.viewRelevant = viewRelevant;
	}

	public void setName(String name) {
		this.uid = name;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public String getUID() {
		return uid;
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
	
	@JsonIgnore
	@Override
	public Object getGeneral() {
		return get(null, null, 0, 0);
	}

	@JsonIgnore
	@Override
	public <C> C getGeneral(Class<C> cls) {
		Object obj = getGeneral();
		if (obj == null)
			return null;
		return cast(obj, cls);
	}
	
	@Override
	public <C> C get(SystemContext systemContext, Class<C> valueCls, ComplexNumber chunkPos, int pixel, int sample) {
		Object obj = get(systemContext, chunkPos, pixel, sample);
		if (obj == null)
			return null;
		return cast(obj, valueCls);
	}
	
	@SuppressWarnings("unchecked")
	private <C> C cast(Object object, Class<C> cls) {
		if (cls.isInstance(object))
			return (C)object;
		throw new IllegalArgumentException("object can't be cast to class");
	}
	
	public void setAttr(int state) {
		changed = (state & ATTR_CHANGED) > 0 ? true : false;
		systemRelevant = (state & ATTR_SYSTEM_RELEVANT) > 0 ? true : false;
		layerRelevant = (state & ATTR_LAYER_RELEVANT) > 0 ? true : false;
		viewRelevant = (state & ATTR_VIEW_RELEVANT) > 0 ? true : false;
	}
	
	public int getAttr() {
		int val = changed ? ATTR_CHANGED : 0;
		val |= systemRelevant ? ATTR_SYSTEM_RELEVANT : 0;
		val |= layerRelevant ? ATTR_LAYER_RELEVANT : 0;
		val |= viewRelevant ? ATTR_VIEW_RELEVANT : 0;
		return val;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractParamSupplier other = (AbstractParamSupplier) obj;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
}
