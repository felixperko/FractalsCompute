package de.felixperko.fractals.data;

import java.io.Serializable;

import de.felixperko.fractals.system.task.FractalsTask;

public interface Chunk extends Serializable{

	double getValue(int i);

	double getValue(int i, boolean strict);

	void addSample(int i, double value, int upsample);

	int getIndex(int chunkX, int chunkY);

//	int getArrayLength();

//	int getChunkSize();

	Integer getChunkX();

	Integer getChunkY();

//	int getDimensionSize();
//
//	void setDimensionSize(int dimensionSize);

	int getSampleCount(int i);

	void setChunkX(Integer chunkX);

	void setChunkY(Integer chunkY);

//	void setArrayLength(int arrayLength);

	double distanceSq(double otherX, double otherY);

	double distance(double otherX, double otherY);

	FractalsTask getCurrentTask();

	void setCurrentTask(FractalsTask currentTask);
	
	int getJobId();
	
	void setJobId(int jobId);

}