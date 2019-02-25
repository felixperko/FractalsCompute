package de.felixperko.fractals.data;

import java.io.Serializable;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.task.FractalsTask;

public class Chunk implements Serializable{
	
	private static final long serialVersionUID = -6507259803639466582L;

	Long chunkX, chunkY;
	
	int dimensionSize;
	int arrayLength;
	int sampleCount;
	
	double[] values;
	int[] failedSamples;
	
	public ComplexNumber chunkPos;
	
	FractalsTask currentTask;
	
	public Chunk(long chunkX, long chunkY, int dimensionSize) {
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

	public Long getChunkX() {
		return chunkX;
	}
	
	public Long getChunkY() {
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

	public void setChunkX(Long chunkX) {
		this.chunkX = chunkX;
	}

	public void setChunkY(Long chunkY) {
		this.chunkY = chunkY;
	}

	public void setArrayLength(int arrayLength) {
		this.arrayLength = arrayLength;
	}
	
	public double distanceSq(double otherX, double otherY) {
		double dx = otherX-chunkX;
		double dy = otherY-chunkY;
		return dx*dx + dy*dy;
	}
	
	public double distance(double otherX, double otherY) {
		return Math.sqrt(distanceSq(otherX, otherY));
	}

	
	public FractalsTask getCurrentTask() {
		return currentTask;
	}
	
	public void setCurrentTask(FractalsTask currentTask) {
		this.currentTask = currentTask;
	}
}
