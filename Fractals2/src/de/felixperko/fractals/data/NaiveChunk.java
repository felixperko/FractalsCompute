package de.felixperko.fractals.data;

import java.io.Serializable;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.task.FractalsTask;

public class NaiveChunk extends AbstractArrayChunk{
	
	private static final long serialVersionUID = -6507259803639466582L;
	
	double[] values;
	int[] samples;
	int[] failedSamples;
	// 64 bit + 2 * 32 bit -> 16 byte per pixel
	
	NaiveChunk(int chunkX, int chunkY, int dimensionSize) {
		super(chunkX, chunkY, dimensionSize);
		this.values = new double[arrayLength];
		this.samples = new int[arrayLength];
		this.failedSamples = new int[arrayLength];
	}
	
	@Override
	public double getValue(int i) {
		return getValue(i, false);
	}
	
	@Override
	public double getValue(int i, boolean strict) {
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
				i = (x + upstep-1)*dimensionSize + (y + upstep-1);
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
	public void addSample(int i, double value) {
		if (value < 0)
			failedSamples[i]++;
		else
			values[i] += value;
		samples[i]++;
	}

	@Override
	public int getSampleCount(int i) {
		return samples[i];
	}
}
