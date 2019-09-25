package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;
import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

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