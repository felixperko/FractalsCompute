package de.felixperko.fractals.network;

import java.io.Serializable;
import java.util.Map;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class SystemClientData extends ParamContainer implements Serializable{
	
	private static final long serialVersionUID = -6322484739454792244L;
	
	int grantThreads;
	
	public SystemClientData(Map<String, ParamSupplier> clientParameters, int grantThreads) {
		super(clientParameters);
		this.grantThreads = grantThreads;
	}
	
	public SystemClientData() {
		
	}

	public int getGrantedThreads() {
		return grantThreads;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientParameters == null) ? 0 : clientParameters.hashCode());
		result = prime * result + grantThreads;
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
		SystemClientData other = (SystemClientData) obj;
		if (clientParameters == null) {
			if (other.clientParameters != null)
				return false;
		} else if (!clientParameters.equals(other.clientParameters))
			return false;
		if (grantThreads != other.grantThreads)
			return false;
		return true;
	}
}
