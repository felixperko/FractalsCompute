package de.felixperko.fractals.system.parameters.suppliers;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.util.serialization.jackson.JsonObjectDeserializer;

public class StaticParamSupplier extends AbstractParamSupplier {
	
	private static final long serialVersionUID = 8842788371106789651L;
	
	@JsonDeserialize(using = JsonObjectDeserializer.class)
	Object obj;
	
	public StaticParamSupplier() {
		super(null);
	}
	
	public StaticParamSupplier(String name, Object obj) {
		super(name);
		this.obj = obj;
	}

	@Override
	public Object get(SystemContext systemContext, ComplexNumber chunkPos, int pixel, int sample) {
		return obj;
	}

	@Override
	public ParamSupplier copy() {
		ParamSupplier other = null;
		if (obj instanceof Copyable<?>)
			other =  new StaticParamSupplier(name, ((Copyable) obj).copy());
		else
			other = new StaticParamSupplier(name, obj);
		other.setLayerRelevant(layerRelevant).setSystemRelevant(systemRelevant);
		return other;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
		StaticParamSupplier other = (StaticParamSupplier) obj;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		return true;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
