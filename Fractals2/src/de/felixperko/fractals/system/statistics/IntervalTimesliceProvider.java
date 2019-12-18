package de.felixperko.fractals.system.statistics;

import de.felixperko.fractals.util.NumberUtil;

public class IntervalTimesliceProvider implements TimesliceProvider {
	
	long startTime;
	long intervalInNs;
	
	public IntervalTimesliceProvider(int startTime, long intervalInNs) {
		this.startTime = startTime;
		this.intervalInNs = intervalInNs;
	}
	
	public IntervalTimesliceProvider(double intervalInS) {
		this.startTime = System.nanoTime();
		this.intervalInNs = Math.round(NumberUtil.S_TO_NS*intervalInS);
	}
	
	@Override
	public int getCurrentTimeslice() {
		long d = System.nanoTime() - startTime;
		return (int) (d/intervalInNs);
	}

	@Override
	public long getStartTime(int timeslice) {
		return startTime + intervalInNs*timeslice;
	}

	@Override
	public long getEndTime(int timeslice) {
		return startTime + intervalInNs*(timeslice+1);
	}
}
