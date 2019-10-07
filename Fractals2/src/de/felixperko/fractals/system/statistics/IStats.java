package de.felixperko.fractals.system.statistics;

import java.io.Serializable;

public interface IStats extends Serializable{
	public void addSample(int iterations, double result);
	public void addCulled();
	public void executionStart();
	public void executionEnd();
	public long getExecutionTimeNS();
	public int getIterationsPerSecondSinceStart();
}
