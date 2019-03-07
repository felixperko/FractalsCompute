package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;

import de.felixperko.fractals.system.task.Layer;

public class BreadthFirstLayer implements Layer {
	
	private static final long serialVersionUID = -2680536406773328635L;
	
	int id;
	double priority_multiplier = 1;
	double priority_shift = 0;
	BitSet enabledPixels = null;
	int samples = 1;
	
	public BreadthFirstLayer(int id) {
		this.id = id;
	}
	
	@Override
	public boolean isActive(int pixel) {
		if (enabledPixels == null)
			return true;
		return enabledPixels.get(pixel);
	}
	
	public BreadthFirstLayer with_priority_multiplier(double priority_multiplier) {
		this.priority_multiplier = priority_multiplier;
		return this;
	}

	public BreadthFirstLayer with_priority_shift(double priority_shift) {
		this.priority_shift = priority_shift;
		return this;
	}

	public BreadthFirstLayer with_enabled_pixels(BitSet enabledPixels) {
		this.enabledPixels = enabledPixels;
		return this;
	}
	
	public BreadthFirstLayer with_samples(int sampleCount) {
		this.samples = sampleCount;
		return this;
	}
	
//	public GlobalPixel getNeighbour(int pixel, int chunkSize, int shiftX, int shiftY) {
//		//TODO or share border data either way?
//		if (enabledPixels == null) {
//			int x = pixel / chunkSize + shiftX;
//			int y = pixel % chunkSize + shiftY;
//			if (x < 0) {
//				if (y < 0)
//					return new GlobalPixel(-1, -1, chunkSize+x, chunkSize+y);
//				return new GlobalPixel(-1, 0, chunkSize+x, y);
//			}
//			if (y < 0)
//				return new GlobalPixel(0, -1, x, chunkSize+y);
//			return new GlobalPixel(0, 0, x, y);
//		} else {
//			throw new IllegalStateException("no implemented neighbour bahavior");
//		}
//	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public double getPriorityMultiplier() {
		return priority_multiplier;
	}
	
	@Override
	public double getPriorityShift() {
		return priority_shift;
	}

	@Override
	public int getSampleCount() {
		return samples;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof BreadthFirstLayer))
			return false;
		BreadthFirstLayer o = (BreadthFirstLayer) other;
		return (o.id == id && o.samples == samples && ((enabledPixels == null && o.enabledPixels == null) || o.enabledPixels.equals(enabledPixels)));
	}
}
