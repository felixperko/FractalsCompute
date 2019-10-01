package de.felixperko.fractals.system.calculator.infra;

import java.io.Serializable;

import de.felixperko.fractals.system.numbers.ComplexNumber;

public class SingleTraceListener implements TraceListener, Serializable {

	private static final long serialVersionUID = 825265145156905403L;

	int viewId;
	int chunkX, chunkY;
	int pixel;
	
	public SingleTraceListener(int viewId, int chunkX, int chunkY, int pixel) {
		this.viewId = viewId;
		this.pixel = pixel;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
	}
	
	@Override
	public void trace(ComplexNumber number, int pixel, int sample, int iteration) {
		if (pixel != this.pixel)
			return;
		
		//TODO do something
	}

	@Override
	public boolean isApplicable(int viewId, int chunkX, int chunkY) {
		return chunkX == this.chunkX && chunkY == this.chunkY && viewId == this.viewId;
	}

}
