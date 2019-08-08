package de.felixperko.fractals.system.task;

import java.io.Serializable;
import java.util.UUID;

import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.statistics.TaskStats;
import de.felixperko.fractals.system.thread.FractalsThread;

public interface FractalsTask extends Serializable{

	TaskManager<?> getTaskManager();
	
	Integer getId();
	Integer getJobId();
	UUID getSystemId();

	TaskStateInfo getStateInfo();
	TaskState getState();
	
	public void setThread(FractalsThread thread);
	
	public FractalsCalculator getCalculator();
	
	public void run() throws InterruptedException;

	
	public TaskStats getTaskStats();
	public void setTaskStats(TaskStats taskStats);

	void setStateInfo(TaskStateInfo taskStateInfo);

	SystemContext getContext();
	
}
