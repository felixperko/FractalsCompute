package de.felixperko.fractals.system.statistics;

public interface IHistogramStats {
	
	int getIterationCount();
	int[] getIterationHistogram();
	long getExecutionTimeNS();
	int getBinWidth();
	int getMaxIterations();
	boolean isInitialized();
}
