package de.felixperko.fractals.system.task.statistics;

import de.felixperko.fractals.system.task.Layer;

public interface TaskStats {
	public void addSample(float value, long startTime, Layer currentLayer);
	public void addCulled(float value, Layer currentLayer);
	public void removeCulled(float value, Layer currentLayer);
}