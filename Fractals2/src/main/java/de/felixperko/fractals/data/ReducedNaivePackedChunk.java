package de.felixperko.fractals.data;

public class ReducedNaivePackedChunk extends AbstractArrayChunk{

	private static final long serialVersionUID = -5680093357484529878L;
	
	float[] values;
	int[] samples;
	int[] failedSamples;
	
	int chunkX;
	int chunkY;
	int dimensionSize;
	int upsample;
	
	protected ReducedNaivePackedChunk(int chunkX, int chunkY, int dimensionSize, float[] values, int[] samples, int[] failedSamples, int upsample) {
		super(null, chunkX, chunkY, dimensionSize);
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.dimensionSize = dimensionSize;
		this.values = values;
		this.samples = samples;
		this.failedSamples = failedSamples;
		this.upsample = upsample;
	}

	public float[] getValues() {
		return values;
	}

	public int[] getSamples() {
		return samples;
	}

	public int[] getFailedSamples() {
		return failedSamples;
	}
	
	@Override
	public Integer getChunkX() {
		return chunkX;
	}

	@Override
	public Integer getChunkY() {
		return chunkY;
	}

	public int getDimensionSize() {
		return dimensionSize;
	}

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
	public int getStartIndex() {
		return ((upsample/2+1) * (dimensionSize+1))*upsample;
	}
	
	@Override
	public void addSample(int i, double value, int upsample) {
		throw new IllegalStateException("ReducedNaivePackedChunk doesn't implement this method.");
	}

	@Override
	public int getSampleCount(int i) {
		throw new IllegalStateException("ReducedNaivePackedChunk doesn't implement this method.");
	}

	@Override
	protected void removeFlag(int i) {
		throw new IllegalStateException("ReducedNaivePackedChunk doesn't implement this method.");
	}

	@Override
	public int getFailedSampleCount(int i) {
		// TODO Auto-generated method stub
		return 0;
	}
}
