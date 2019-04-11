package de.felixperko.fractals.system.task.statistics;

import de.felixperko.fractals.system.task.Layer;

public class TaskStatsEmpty implements TaskStats {

	@Override
	public void addSample(float value, long startTime, Layer currentLayer) {
	}

	@Override
	public void addCulled(float value, Layer currentLayer) {
	}

	@Override
	public void removeCulled(float value, Layer currentLayer) {
	}

}
