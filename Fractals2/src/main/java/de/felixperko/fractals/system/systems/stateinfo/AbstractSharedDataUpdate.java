package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;

import de.felixperko.fractals.data.shareddata.SharedDataUpdate;

public abstract class AbstractSharedDataUpdate implements SharedDataUpdate, Serializable{
	
	private static final long serialVersionUID = 224161803518559660L;
	
	boolean sent = false;
	
	public AbstractSharedDataUpdate() {
	}
	
	@Override
	public boolean isSent() {
		return sent;
	}

	@Override
	public void setSent() {
		this.sent = true;
	}

}
