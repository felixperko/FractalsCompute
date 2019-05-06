package de.felixperko.fractals.data;

public class ReducedNaivePackedChunk extends AbstractArrayChunk{

	private static final long serialVersionUID = -5680093357484529878L;
	
	float[] values;
	byte[] samples;
	byte[] failedSamples;
	
	int chunkX;
	int chunkY;
	int dimensionSize;
	
	protected ReducedNaivePackedChunk(int chunkX, int chunkY, int dimensionSize, float[] values, byte[] samples, byte[] failedSamples) {
		super(null, chunkX, chunkY, dimensionSize);
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.dimensionSize = dimensionSize;
		this.values = values;
		this.samples = samples;
		this.failedSamples = failedSamples;
	}

	public float[] getValues() {
		return values;
	}

	public byte[] getSamples() {
		return samples;
	}

	public byte[] getFailedSamples() {
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
		throw new IllegalStateException("ReducedNaivePackedChunk doesn't implement this method.");
	}

	@Override
	public double getValue(int i, boolean strict) {
		throw new IllegalStateException("ReducedNaivePackedChunk doesn't implement this method.");
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
}
