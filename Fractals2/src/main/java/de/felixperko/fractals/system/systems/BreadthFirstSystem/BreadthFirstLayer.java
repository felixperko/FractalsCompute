package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.serialization.jackson.JsonAbstractTypedObject;

public class BreadthFirstLayer extends JsonAbstractTypedObject implements Layer {
	
	public static final String TYPE_NAME = "bfLayer";
	
	private static final long serialVersionUID = -2680536406773328635L;
	
	int id = -1;
	double priorityMultiplier = 1;
	double priorityShift = 0;
	@JsonIgnore
	transient EnabledPixels enabledPixels = null;
	int sampleCount = 1;
	int maxIterations = -1;
	
	int chunkSize = -1;
	
	boolean culling = false;
	
	boolean rendering = true;
	
	public BreadthFirstLayer() {
		super(TYPE_NAME);
	}
	
	public BreadthFirstLayer(int chunkSize) {
		super(TYPE_NAME);
		this.chunkSize = chunkSize;
	}
	
	public BreadthFirstLayer(String subClassTypeName) {
		super(subClassTypeName);
	}

	public BreadthFirstLayer(String subClassTypeName, int chunkSize) {
		super(subClassTypeName);
		this.chunkSize = chunkSize;
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
		return getEnabledPixels().isEnabled(pixel);
	}
	
	public BreadthFirstLayer with_priority_multiplier(double priority_multiplier) {
		this.priorityMultiplier = priority_multiplier;
		return this;
	}

	public BreadthFirstLayer with_priority_shift(double priority_shift) {
		this.priorityShift = priority_shift;
		return this;
	}
	
	public BreadthFirstLayer with_samples(int sampleCount) {
		this.sampleCount = sampleCount;
		return this;
	}
	
	public BreadthFirstLayer with_rendering(boolean rendering) {
		this.rendering = rendering;
		return this;
	}
	
	public BreadthFirstLayer with_max_iterations(int maxIterations) {
		this.maxIterations = maxIterations;
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
		return priorityMultiplier;
	}
	
	@Override
	public double getPriorityShift() {
		return priorityShift;
	}

	@Override
	public int getSampleCount() {
		return sampleCount;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (culling ? 1231 : 1237);
		result = prime * result + ((enabledPixels == null) ? 0 : enabledPixels.hashCode());
		result = prime * result + id;
		long temp;
		temp = Double.doubleToLongBits(priorityMultiplier);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(priorityShift);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (rendering ? 1231 : 1237);
		result = prime * result + sampleCount;
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
		EnabledPixels ep = getEnabledPixels();
		EnabledPixels ep2 = other.getEnabledPixels();
		if (ep == null) {
			if (ep2 != null)
				return false;
		} else if (!ep.equals(ep2))
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(priorityMultiplier) != Double.doubleToLongBits(other.priorityMultiplier))
			return false;
		if (Double.doubleToLongBits(priorityShift) != Double.doubleToLongBits(other.priorityShift))
			return false;
		if (rendering != other.rendering)
			return false;
		if (sampleCount != other.sampleCount)
			return false;
		return true;
	}

	@Override
	public int getUpsample() {
		return 1;
	}
	
	public void setUpsample(int upsample) {
		if (upsample != 1)
			throw new IllegalArgumentException();
	}

	@Override
	public int getMaxIterations() {
		return maxIterations;
	}

	public void setPriorityMultiplier(double priorityMultiplier) {
		this.priorityMultiplier = priorityMultiplier;
	}

	public void setPriorityShift(double priorityShift) {
		this.priorityShift = priorityShift;
	}

	public EnabledPixels getEnabledPixels() {
		if (enabledPixels == null)
			enabledPixels = initEnabledPixels();
		return enabledPixels;
	}

	protected EnabledPixels initEnabledPixels() {
		return new DefaultEnabledPixels(chunkSize);
	}

	public int getSamples() {
		return sampleCount;
	}

	public void setSamples(int samples) {
		this.sampleCount = samples;
	}

	public boolean isCulling() {
		return culling;
	}

	public void setCulling(boolean culling) {
		this.culling = culling;
	}

	public boolean isRendering() {
		return rendering;
	}

	public void setRendering(boolean rendering) {
		this.rendering = rendering;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
	
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	
}
