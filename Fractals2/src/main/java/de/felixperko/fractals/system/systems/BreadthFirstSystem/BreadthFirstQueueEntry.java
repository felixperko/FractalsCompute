package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import de.felixperko.fractals.system.task.Layer;

public interface BreadthFirstQueueEntry {
	public int getLayerId();
	public void updatePriorityAndDistance(double midpointChunkX, double midpointChunkY, Layer layer);
}
