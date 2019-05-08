package de.felixperko.fractals.system.systems.BreadthFirstSystem;

public interface BreadthFirstQueueEntry {
	public int getLayerId();
	public void updatePriorityAndDistance(double midpointChunkX, double midpointChunkY, BreadthFirstLayer layer);
}
