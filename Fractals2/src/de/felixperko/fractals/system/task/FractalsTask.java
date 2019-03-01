package de.felixperko.fractals.system.task;

import java.io.Serializable;

import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;

public interface FractalsTask extends Serializable{

	TaskManager getTaskManager();

	Integer getId();

	TaskStateInfo getStateInfo();
	TaskState getState();
	
	public void run() throws InterruptedException;
	
}
