package de.felixperko.fractals.network;

import java.io.Serializable;

public class SystemClientData implements Serializable{
	
	private static final long serialVersionUID = -6322484739454792244L;
	
	int grantThreads;
	ParamContainer paramContainer;
	
	public SystemClientData(ParamContainer paramContainer, boolean newInstance) {
		if (!newInstance)
			this.paramContainer = paramContainer;
		else {
			this.paramContainer = new ParamContainer(paramContainer, true);
		}
	}
	
	public SystemClientData() {
		
	}

	public int getGrantedThreads() {
		return grantThreads;
	}
	
	public ParamContainer copyParams(boolean newInstance) {
		return new ParamContainer(paramContainer, newInstance);
	}
	
	public ParamContainer getParamContainer() {
		return paramContainer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramContainer == null) ? 0 : paramContainer.hashCode());
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
		if (paramContainer == null) {
			if (other.paramContainer != null)
				return false;
		} else if (!paramContainer.equals(other.paramContainer))
			return false;
		if (grantThreads != other.grantThreads)
			return false;
		return true;
	}
}
