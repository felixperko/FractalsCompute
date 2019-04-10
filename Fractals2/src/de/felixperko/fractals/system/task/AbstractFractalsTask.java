package de.felixperko.fractals.system.task;

import java.util.UUID;

import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;

public abstract class AbstractFractalsTask implements FractalsTask{
	
	private static final long serialVersionUID = -3755610537350804691L;
	
	Integer id;
	transient TaskManager taskManager;
	TaskStateInfo stateInfo;
	int jobId;
	UUID systemId;
	
	public AbstractFractalsTask(Integer id, TaskManager taskManager, Layer layer, int jobId) {
		this.id = id;
		this.jobId = jobId;
		this.systemId = taskManager.getSystem().getId();
		this.taskManager = taskManager;
		this.stateInfo = new TaskStateInfo(id, layer);
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
	
	@Override
	public Integer getJobId() {
		return jobId;
	}
	
	@Override
	public UUID getSystemId() {
		return systemId;
	}
}
