package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ParamContainer implements Serializable{

	private static final long serialVersionUID = 2325163791938639608L;
	
	protected Map<String, ParamSupplier> clientParameters;

	public ParamContainer() {
	}

	public ParamContainer(Map<String, ParamSupplier> clientParameters) {
		this.clientParameters = clientParameters;
	}

	public ParamContainer(ParamContainer parent, boolean newInstances) {
		if (!newInstances)
			this.clientParameters = parent.getClientParameters();
		else {
			this.clientParameters = new HashMap<String, ParamSupplier>();
			for (Entry<String, ParamSupplier> e : parent.getClientParameters().entrySet()){
				this.clientParameters.put(e.getKey(), e.getValue().copy());
			}
		}
	}

	public boolean needsReset(Map<String, ParamSupplier> oldParams){
		boolean reset = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : clientParameters.values()) {
				supplier.updateChanged(oldParams.get(supplier.getName()));
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						reset = true;
				}
			}
		}
		return reset;
	}
	
	public void applyParams(ParamContainer paramContainer) {
		Map<String, ParamSupplier> old = getClientParameters();
		this.clientParameters = new HashMap<>(paramContainer.getClientParameters());
		for (String key : old.keySet())
			if (!this.clientParameters.containsKey(key))
				this.clientParameters.put(key, old.get(key).copy());
	}
	
	public boolean applyParamsAndNeedsReset(ParamContainer paramContainer) {
		Map<String, ParamSupplier> old = getClientParameters();
		applyParams(paramContainer);
		return needsReset(old);
	}

	public void addClientParameter(ParamSupplier paramSupplier) {
		clientParameters.put(paramSupplier.getName(), paramSupplier);
	}

	public boolean hasClientParameters() {
		return clientParameters.size() > 0;
	}

	public ParamSupplier getClientParameter(String name) {
		return clientParameters.get(name);
	}

	public Map<String, ParamSupplier> getClientParameters() {
		return clientParameters;
	}

}