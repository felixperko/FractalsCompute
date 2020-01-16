package de.felixperko.fractals.data.shareddata;

public interface PrioritizedSharedDataUpdate extends SharedDataUpdate, Comparable<PrioritizedSharedDataUpdate> {
	
	public double getPriority();
	
	@Override
	default int compareTo(PrioritizedSharedDataUpdate arg0) {
		return Double.compare(getPriority(), arg0.getPriority());
	}
}
