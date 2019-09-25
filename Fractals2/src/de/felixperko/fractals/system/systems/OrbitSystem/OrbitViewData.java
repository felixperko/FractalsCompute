package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.AbstractBFViewData;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class OrbitViewData implements ViewData<OrbitSystemContext> {

	private static final long serialVersionUID = 7465341398909992740L;
	
	ParamContainer paramContainer;
	OrbitSystemContext context;
	
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBufferTimeout(double seconds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActive(boolean active) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
