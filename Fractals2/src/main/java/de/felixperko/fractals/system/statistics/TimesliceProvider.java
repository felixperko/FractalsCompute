package de.felixperko.fractals.system.statistics;

/**
 * returns the current timeslice for statistics
 */
public interface TimesliceProvider {
	public int getCurrentTimeslice();

	public long getStartTime(int timeslice);
	public long getEndTime(int timeslice);
}
