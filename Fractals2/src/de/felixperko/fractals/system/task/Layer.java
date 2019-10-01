package de.felixperko.fractals.system.task;

import java.io.Serializable;

public interface Layer extends Serializable{
	
	int getId();
	boolean isActive(int pixel);
	double getPriorityMultiplier();
	double getPriorityShift();
	int getSampleCount();
	boolean cullingEnabled();
	boolean renderingEnabled();
	int getUpsample();
	void setId(int id);
	int getMaxIterations();

}