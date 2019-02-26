package de.felixperko.fractals.data;

import java.io.Serializable;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.task.FractalsTask;

public class Chunk implements Serializable{
	
	private static final long serialVersionUID = -6507259803639466582L;

	Long chunkX, chunkY;
	
	int dimensionSize;
	int arrayLength;
	
	double[] values;
	int[] samples;
	int[] failedSamples;
	
	public ComplexNumber chunkPos;
	
	FractalsTask currentTask;
	
	public Chunk(long chunkX, long chunkY, int dimensionSize) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		
		this.dimensionSize = dimensionSize;
		this.arrayLength = dimensionSize*dimensionSize;		
		this.values = new double[arrayLength];
		this.samples = new int[arrayLength];
		this.failedSamples = new int[arrayLength];
	}
	
	public Chunk() {
		
	}
	
	public double getValue(int i) {
		return getValue(i, false);
	}
	
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
	
	public void addSample(int i, double value) {
		if (value < 0)
			failedSamples[i]++;
		else
			values[i] += value;
		samples[i]++;
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

	public int getDimensionSize() {
		return dimensionSize;
	}

	public void setDimensionSize(int dimensionSize) {
		this.dimensionSize = dimensionSize;
	}

	public int getSampleCount(int i) {
		return samples[i];
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
