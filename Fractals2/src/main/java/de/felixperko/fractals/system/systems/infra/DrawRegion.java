package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.BFSystemContext;

public interface DrawRegion<CONTEXT extends SystemContext<?>> {
	
	double getRegionDistance(BFSystemContext context, long chunkX, long chunkY);
	boolean inRegion(CONTEXT context, long chunkX, long chunkY);

}
