package de.felixperko.fractals.data;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class ReducedNaiveChunk extends AbstractArrayChunk {
	
	private static final long serialVersionUID = -8824365910389869969L;
	
	float[] values;
	byte[] samples;
	byte[] failedSamples;
	
	// 32 bit + 2 * 8 bit -> 6 byte per pixel

	ReducedNaiveChunk(ViewData viewData, int chunkX, int chunkY, int dimensionSize) {
		super(null, chunkX, chunkY, dimensionSize);
		this.values = new float[arrayLength];
		this.samples = new byte[arrayLength];
		this.failedSamples = new byte[arrayLength];
	}
	
	protected ReducedNaiveChunk(int chunkX, int chunkY, int dimensionSize, float[] values, byte[] samples, byte[] failedSamples) {
		super(null, chunkX, chunkY, dimensionSize);
		this.values = values;
		this.samples = samples;
		this.failedSamples = failedSamples;
	}
	
	// 64 bit + 2 * 32 bit -> 16 byte per pixel
	
	@Override
	public double getValue(int i) {
		return getValue(i, false);
	}
	
	@Override
	public double getValue(int i, boolean strict) {
		float currValues = values[i];
		if (currValues == FLAG_CULL)
			return FLAG_CULL;
		
		int count = samples[i];
		if (count == 0) {
			if (strict)
				return 0;
			int x = i/dimensionSize;
			int y = i%dimensionSize;
			int upstep = 1;
			while (count == 0) {
				upstep *= 2;
				if (upstep >= dimensionSize)
					return 0;
				x -= x%upstep;
				y -= y%upstep;
				i = (x + upstep/2)*dimensionSize + (y + upstep/2);
				count = samples[i];
				if (count != 0)
					break;
			}
			if (count == 0)
				return 0;
		}
		return values[i] / (samples[i]-failedSamples[i]);
	}
	
	@Override
	public void addSample(int i, double value, int higher) {
		boolean hadValidValue = samples[i] > failedSamples[i];
		boolean hasValidValue = hadValidValue;
		synchronized (this) {
			if (value == FLAG_CULL) {
				if (samples[i] == 0)
					values[i] = -2;
			} else {
				if (value < 0)
					failedSamples[i]++;
				else {
					if (samples[i] == 0) //override flags
						values[i] = (float) value;
					else
						values[i] += value;
					hasValidValue = true;
				}
				samples[i]++;
			}
		}
		
		if (!hadValidValue && hasValidValue) {
			int x = i%dimensionSize;
			int y = i/dimensionSize;
			for (ChunkBorderData data : getIndexBorderData(x, y, upsample)) {
				BorderAlignment alignment = data.getAlignment();
				if (alignment.isHorizontal()) {
					int lower = y-upsample/2-1;
					int higher2 = lower+upsample;
					data.set(hasValidValue, clampIndex(lower), clampIndex(higher2));
				} else {
					int lower = x-upsample/2-1;
					int higher2 = lower+upsample;
					data.set(hasValidValue, clampIndex(lower), clampIndex(higher2));
				}
			}
		}
	}
	
	private int clampIndex(int val) {
		return clamp(val, 0, dimensionSize);
	}
	
	private int clamp(int val, int min, int max) {
		return val < min ? min : (val > max ? max : val);
	}

	@Override
	public int getSampleCount(int i) {
		return samples[i];
	}

	@Override
	protected void removeFlag(int i) {
		samples[i] = 0;
		values[i] = 0;
	}

	@Override
	public int getStartIndex() {
		return 0;
	}

}
