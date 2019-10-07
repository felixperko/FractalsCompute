package de.felixperko.fractals.system.systems.OrbitSystem;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.system.systems.infra.SingleViewData;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class OrbitViewData implements SingleViewData<OrbitSystemContext> {

	private static final long serialVersionUID = 7465341398909992740L;
	
	ParamContainer paramContainer;
	OrbitSystemContext context;
	Chunk chunk;
	
	boolean active;
	
	@Override
	public ParamContainer getParams() {
		return paramContainer;
	}

	public void setParams(ParamContainer paramContainer) {
		this.paramContainer = paramContainer;
	}

	@Override
	public OrbitSystemContext getContext() {
		return context;
	}

	@Override
	public OrbitViewData setContext(OrbitSystemContext systemContext) {
		this.context = systemContext;
		return this;
	}

	@Override
	public void tick() {
		
	}

	@Override
	public void setBufferTimeout(double seconds) {
		
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Chunk getChunk() {
		return chunk;
	}

	@Override
	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}
}
