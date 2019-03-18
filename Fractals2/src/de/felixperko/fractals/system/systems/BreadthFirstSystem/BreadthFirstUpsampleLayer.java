package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;

public class BreadthFirstUpsampleLayer extends BreadthFirstLayer{
	
	private static final long serialVersionUID = -6818101684225388444L;
	
	int upsample;
	
	boolean culling;

	public BreadthFirstUpsampleLayer(int upsample, int chunkSize) {
		super();
		this.upsample = upsample;
		BitSet bitSet = new BitSet();
		for (int x = upsample-1 ; x < chunkSize ; x += upsample) {
			for (int y = upsample-1 ; y < chunkSize ; y += upsample) {
				bitSet.set(x*chunkSize + y);
			}
		}
		with_enabled_pixels(bitSet);
	}
	
	public BreadthFirstUpsampleLayer with_culling(boolean culling) {
		this.culling = culling;
		return this;
	}
	
	public boolean cullingEnabled() {
		return culling;
	}
	
//	@Override
//	public GlobalPixel getNeighbour(int pixel, int chunkSize, int shiftX, int shiftY) {
//		int x = pixel / chunkSize + shiftX*upsample;
//		int y = pixel % chunkSize + shiftY*upsample;
//		if (x < 0) {
//			if (y < 0)
//				return new GlobalPixel(-1, -1, chunkSize+x, chunkSize+y);
//			return new GlobalPixel(-1, 0, chunkSize+x, y);
//		}
//		if (y < 0)
//			return new GlobalPixel(0, -1, x, chunkSize+y);
//		return new GlobalPixel(0, 0, x, y);
//	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof BreadthFirstUpsampleLayer))
			return false;
		BreadthFirstUpsampleLayer o = (BreadthFirstUpsampleLayer) other;
		return (o.id == id && o.upsample == upsample);
	}

	public int getUpsample() {
		return upsample;
	}

	public BitSet getEnabledPixels() {
		return enabledPixels;
	}
}
