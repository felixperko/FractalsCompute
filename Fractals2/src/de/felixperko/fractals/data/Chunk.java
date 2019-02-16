package de.felixperko.fractals.data;

import java.io.Serializable;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public class Chunk implements Serializable{
	
	private static final long serialVersionUID = -6507259803639466582L;

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
	
	public Chunk() {
		
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

	public int getDimensionSize() {
		return dimensionSize;
	}

	public void setDimensionSize(int dimensionSize) {
		this.dimensionSize = dimensionSize;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		this.values = values;
	}

	public int[] getFailedSamples() {
		return failedSamples;
	}

	public void setFailedSamples(int[] failedSamples) {
		this.failedSamples = failedSamples;
	}

	public void setChunkX(int chunkX) {
		this.chunkX = chunkX;
	}

	public void setChunkY(int chunkY) {
		this.chunkY = chunkY;
	}

	public void setArrayLength(int arrayLength) {
		this.arrayLength = arrayLength;
	}
}
