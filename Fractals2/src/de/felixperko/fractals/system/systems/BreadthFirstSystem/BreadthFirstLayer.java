package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;

import de.felixperko.fractals.system.task.Layer;

public class BreadthFirstLayer implements Layer {
	
	private static final long serialVersionUID = -2680536406773328635L;
	
	int id = -1;
	double priority_multiplier = 1;
	double priority_shift = 0;
	BitSet enabledPixels = null;
	int samples = 1;
	
	boolean culling = false;
	
	boolean rendering = true;
	
	public BreadthFirstLayer() {
	}
	
	public BitSet getEnabledBitSet(){
		return enabledPixels;
	}
	
	public BreadthFirstLayer with_culling(boolean culling) {
		this.culling = culling;
		return this;
	}
	
	@Override
	public boolean cullingEnabled() {
		return culling;
	}
	
	public boolean renderingEnabled() {
		return rendering;
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
	
	public BreadthFirstLayer with_rendering(boolean rendering) {
		this.rendering = rendering;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (culling ? 1231 : 1237);
		result = prime * result + ((enabledPixels == null) ? 0 : enabledPixels.hashCode());
		result = prime * result + id;
		long temp;
		temp = Double.doubleToLongBits(priority_multiplier);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(priority_shift);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (rendering ? 1231 : 1237);
		result = prime * result + samples;
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreadthFirstLayer other = (BreadthFirstLayer) obj;
		if (culling != other.culling)
			return false;
		if (enabledPixels == null) {
			if (other.enabledPixels != null)
				return false;
		} else if (!enabledPixels.equals(other.enabledPixels))
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(priority_multiplier) != Double.doubleToLongBits(other.priority_multiplier))
			return false;
		if (Double.doubleToLongBits(priority_shift) != Double.doubleToLongBits(other.priority_shift))
			return false;
		if (rendering != other.rendering)
			return false;
		if (samples != other.samples)
			return false;
		return true;
	}

	@Override
	public int getUpsample() {
		return 1;
	}
	
}
