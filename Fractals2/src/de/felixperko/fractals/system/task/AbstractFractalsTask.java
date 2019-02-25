package de.felixperko.fractals.system.task;

import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;

public abstract class AbstractFractalsTask implements FractalsTask{
	
	private static final long serialVersionUID = -3755610537350804691L;
	
	Integer id;
	transient TaskManager taskManager;
	TaskStateInfo stateInfo;
	
	public AbstractFractalsTask(Integer id, TaskManager taskManager) {
		this.id = id;
		this.taskManager = taskManager;
		this.stateInfo = new TaskStateInfo(id);
		taskManager.getSystem().getSystemStateInfo().addTaskStateInfo(stateInfo);
	}

	public Integer getId() {
		return id;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}
	
	@Override
	public TaskStateInfo getStateInfo() {
		return stateInfo;
	}
	
	@Override
	public TaskState getState() {
		return stateInfo.getState();
	}
}
