package de.felixperko.fractals.system.calculator.infra;

import de.felixperko.fractals.system.numbers.ComplexNumber;

public interface TraceListener {
	boolean isApplicable(int viewId, int chunkX, int chunkY);
	public void trace(ComplexNumber number, int pixel, int sample, int iteration);
}
