package de.felixperko.fractals.data;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public class Chunk {
	
	int chunkX, chunkY;
	
	int dimensionSize;
	int arrayLength;
	int sampleCount;
	
	double[] values;
	int[] failedSamples;
	
	public Chunk(int chunkX, int chunkY, int dimensionSize) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		
		this.dimensionSize = dimensionSize;
		this.arrayLength = dimensionSize*dimensionSize;
		this.sampleCount = 0;
		
		this.values = new double[arrayLength];
		this.failedSamples = new int[arrayLength];
	}
	
	public double getValue(int i) {
		return values[i] / (sampleCount-failedSamples[i]);
	}
	
	public void addSample(int i, double value) {
		if (value < 0)
			failedSamples[i]++;
		else
			values[i] += value;
	}
	
	public int getIndex(int chunkX, int chunkY) {
		return chunkX*dimensionSize + chunkY;
	}
	
	public int getArrayLength() {
		return arrayLength;
	}
	
	public int getChunkSize() {
		return dimensionSize;
	}

	public int getChunkX() {
		return chunkX;
	}
	
	public int getChunkY() {
		return chunkY;
	}
	

	public void incrementSampleCount(int i) {
		sampleCount += i;
	}
}
