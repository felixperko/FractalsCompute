package de.felixperko.fractals.system.task;

import java.util.UUID;

import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.statistics.TaskStats;
import de.felixperko.fractals.system.task.statistics.TaskStatsEmpty;

public abstract class AbstractFractalsTask<T> implements FractalsTask{
	
	private static final long serialVersionUID = -3755610537350804691L;
	
	Integer id;
	transient TaskManager<T> taskManager;
	TaskStateInfo stateInfo;
	int jobId;
	UUID systemId;
	
	TaskStats taskStats = new TaskStatsEmpty();
	
	public AbstractFractalsTask(Integer id, TaskManager<T> taskManager, int jobId, Layer layer) {
		this.id = id;
		this.jobId = jobId;
		this.taskManager = taskManager;
		this.systemId = taskManager.getSystem().getId();
		this.stateInfo = new TaskStateInfo(id);
		stateInfo.setLayer(layer);
		taskManager.getSystem().getSystemStateInfo().addTaskStateInfo(stateInfo);
	}

	public Integer getId() {
		return id;
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

	public TaskStats getTaskStats() {
		return taskStats;
	}

	public void setTaskStats(TaskStats taskStats) {
		this.taskStats = taskStats;
	}
	
	@Override
	public TaskManager<?> getTaskManager() {
		return taskManager;
	}
}
