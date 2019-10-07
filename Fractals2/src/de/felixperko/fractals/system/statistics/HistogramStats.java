package de.felixperko.fractals.system.statistics;

import de.felixperko.fractals.util.NumberUtil;

public class HistogramStats implements IStats, IHistogramStats {
	
	private static final long serialVersionUID = -6580791494809928165L;
	
	int binWidth;
	int maxIterations;

	int iterationCount;
	int[] iterationHistogram; //first index culled, last index failed, in between -> 1 + iterations/binWidth
	
	int[] secondIterationCount;

	int executionTimeNS;
	
	long executionStart;
	
	boolean initialized = false;
	
	public HistogramStats() {
		
	}
	
	public HistogramStats(int binWidth, int maxIterations) {
		
		init(binWidth, maxIterations);
	}
	
	protected void init(int binWidth, int maxIterations) {
		this.binWidth = binWidth;
		this.maxIterations = maxIterations;
		this.iterationHistogram = new int[Math.round(maxIterations/(float)binWidth)+2];
		initialized = true;
	}
	
	private int getIndex(int iterations, double result) {
		if (result > 0)
			return 1 + iterations/binWidth;
		if (result == -2)
			return 0;
		return iterationHistogram.length-1;
	}

	@Override
	public void addSample(int iterations, double result) {
		checkInit();
		this.iterationHistogram[getIndex(iterations, result)]++;
	}

	@Override
	public void addCulled() {
		checkInit();
		iterationHistogram[0]++;
	}
	
	@Override
	public int getIterationCount() {
		checkInit();
		return iterationCount;
	}
	
	@Override
	public int[] getIterationHistogram() {
		checkInit();
		return iterationHistogram;
	}

	@Override
	public int getBinWidth() {
		checkInit();
		return binWidth;
	}

	@Override
	public int getMaxIterations() {
		checkInit();
		return maxIterations;
	}

	@Override
	public void executionStart() {
		checkInit();
		this.executionStart = System.nanoTime();
	}
	
	private void checkInit() {
		if (!initialized)
			throw new IllegalStateException("The histogram must be initialized first");
	}

	@Override
	public void executionEnd() {
		if (executionStart == 0)
			throw new IllegalStateException("Execution wasn't started.");
		this.executionTimeNS = (int)(System.nanoTime() - executionStart);
	}

	@Override
	public long getExecutionTimeNS() {
		return executionTimeNS;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public int getIterationsPerSecondSinceStart() {
		double timeInS = NumberUtil.NS_TO_S*(System.nanoTime()-executionStart);
		return (int)(iterationCount/timeInS);
	}

}
