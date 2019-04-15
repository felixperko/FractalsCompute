package de.felixperko.fractals.network;

import java.io.Serializable;
/**
 * Information about the sender
 */
public class SenderInfo implements Serializable{
	
	private static final long serialVersionUID = 3901678919826037748L;
	
	String name;
	int clientId;
	
	public SenderInfo(int clientId) {
		this.name = "Client_"+clientId;
		this.clientId = clientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clientId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SenderInfo other = (SenderInfo) obj;
		if (clientId != other.clientId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
