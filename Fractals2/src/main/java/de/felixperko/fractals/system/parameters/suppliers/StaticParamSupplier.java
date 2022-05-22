package de.felixperko.fractals.system.parameters.suppliers;

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
	
	public StaticParamSupplier(String uid, Object obj) {
		super(uid);
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
			other =  new StaticParamSupplier(uid, ((Copyable) obj).copy());
		else
			other = new StaticParamSupplier(uid, obj);
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
			StaticParamSupplier oldStatic = (StaticParamSupplier)old;
			Object oldObj = oldStatic.getGeneral();
			return !obj.equals(oldObj);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
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
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" (uid="+getUID()+" obj="+obj+")";
	}
}
