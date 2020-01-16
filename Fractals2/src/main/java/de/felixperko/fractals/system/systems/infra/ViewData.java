package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;

import de.felixperko.fractals.data.ParamContainer;

public interface ViewData<CONTEXT> extends Serializable{
	
	CONTEXT getContext();
	ViewData<CONTEXT> setContext(CONTEXT systemContext);
	
	ParamContainer getParams();
	
	void tick();
	
	void setBufferTimeout(double seconds);
	
	boolean isActive();
	void setActive(boolean active);
	
	void dispose();
}