package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.DrawRegion;

public class BFScreenDrawRegion implements DrawRegion<BFSystemContext> {

	public transient ComplexNumber leftLowerCorner;
	public transient ComplexNumber rightUpperCorner;
	
	double leftLowerCornerChunkX;
	double leftLowerCornerChunkY;
	
	double rightUpperCornerChunkX;
	double rightUpperCornerChunkY;
	
	@Override
	public boolean inRegion(BFSystemContext context, long chunkX, long chunkY) {
		return chunkX+1 >= leftLowerCornerChunkX && chunkY+1 >= leftLowerCornerChunkY &&
				chunkX <= rightUpperCornerChunkX && chunkY <= rightUpperCornerChunkY;
	}
	
	@Override
	public double getRegionDistance(BFSystemContext context, long chunkX, long chunkY) {
		if (chunkX+1 >= leftLowerCornerChunkX && chunkY+1 >= leftLowerCornerChunkY) {
			if (chunkX <= rightUpperCornerChunkX && chunkY <= rightUpperCornerChunkY) {
				return 0;
			}
			double dx = chunkX > rightUpperCornerChunkX ? chunkX-rightUpperCornerChunkX : 0;
			double dy = chunkY > rightUpperCornerChunkY ? chunkY-rightUpperCornerChunkY : 0;
			return Math.sqrt(dx*dx + dy*dy);
		}
		chunkX++;
		chunkY++;
		double dx = chunkX < leftLowerCornerChunkX ? leftLowerCornerChunkX-chunkX : 0;
		double dy = chunkY < leftLowerCornerChunkY ? leftLowerCornerChunkY-chunkY : 0;
		return Math.sqrt(dx*dx + dy*dy);
	}

}
