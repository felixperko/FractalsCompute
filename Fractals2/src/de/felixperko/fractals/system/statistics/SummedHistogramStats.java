package de.felixperko.fractals.system.statistics;

import java.util.List;

public class SummedHistogramStats extends HistogramStats{

	private static final long serialVersionUID = 3760814828786000493L;
	
	public SummedHistogramStats() {
	}
	
	public SummedHistogramStats(IHistogramStats stats) {
		addHistrogramStats(stats);
	}

	public SummedHistogramStats(List<IHistogramStats> histogramTaskStats) {
		if (!histogramTaskStats.isEmpty()) {
			init(histogramTaskStats.get(0).getBinWidth(), histogramTaskStats.get(0).getMaxIterations());
			for (IHistogramStats stats : histogramTaskStats) {
				addHistrogramStats(stats);
			}
		}
	}
	
	public void addHistogram(IHistogramStats stats) {
		addHistrogramStats(stats);
	}

	private void addHistrogramStats(IHistogramStats stats) {
		
		if (!initialized)
			init(stats.getBinWidth(), stats.getMaxIterations());
		
		int[] itHist = stats.getIterationHistogram();
		
		if (stats.getBinWidth() != getBinWidth() || itHist.length != this.iterationHistogram.length)
			throw new IllegalArgumentException("Histograms with varying parameters can't be summed");
		
		for (int i = 0 ; i < itHist.length ; i++) {
			this.iterationHistogram[i] += itHist[i];
		}
		
		executionTimeNS += stats.getExecutionTimeNS();
	}
	
	public void reset() {
		initialized = false;
		executionStart = 0;
		executionTimeNS = 0;
		iterationCount = 0;
		iterationHistogram = null;
	}
}
