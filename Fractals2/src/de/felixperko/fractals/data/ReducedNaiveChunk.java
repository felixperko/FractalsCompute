package de.felixperko.fractals.data;

public class ReducedNaiveChunk extends AbstractArrayChunk {
	
	private static final long serialVersionUID = -8824365910389869969L;
	
	float[] values;
	byte[] samples;
	byte[] failedSamples;
	// 32 bit + 2 * 8 bit -> 6 byte per pixel

	ReducedNaiveChunk(int chunkX, int chunkY, int dimensionSize) {
		super(chunkX, chunkY, dimensionSize);
		this.values = new float[arrayLength];
		this.samples = new byte[arrayLength];
		this.failedSamples = new byte[arrayLength];
	}
	
	// 64 bit + 2 * 32 bit -> 16 byte per pixel
	
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
