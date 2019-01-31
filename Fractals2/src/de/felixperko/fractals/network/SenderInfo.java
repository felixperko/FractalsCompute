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
}
