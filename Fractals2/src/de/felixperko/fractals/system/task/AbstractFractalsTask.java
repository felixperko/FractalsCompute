package de.felixperko.fractals.system.task;

import java.util.UUID;

import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.statistics.TaskStats;
import de.felixperko.fractals.system.task.statistics.TaskStatsEmpty;

public abstract class AbstractFractalsTask<T> implements FractalsTask{
	
	private static final long serialVersionUID = -3755610537350804691L;

	SystemContext context;
	transient TaskManager<T> taskManager;
	
	int jobId;
	
	TaskStateInfo stateInfo;
	
	TaskStats taskStats = new TaskStatsEmpty();
	
	public AbstractFractalsTask(Integer id, TaskManager<T> taskManager, int jobId, Layer layer) {
		this.jobId = jobId;
		this.taskManager = taskManager;
		this.stateInfo = new TaskStateInfo(id, taskManager.getSystem().getId());
		stateInfo.setLayer(layer);
		taskManager.getSystem().getSystemStateInfo().addTaskStateInfo(stateInfo);
	}

	@Override
	public SystemContext getContext() {
		return context;
	}
	
	public void setContext(SystemContext context) {
		this.context = context;
	}

	public Integer getId() {
		return stateInfo.getTaskId();
	}
	
	@Override
	public TaskStateInfo getStateInfo() {
		return stateInfo;
	}

	public void setStateInfo(TaskStateInfo stateInfo) {
		this.stateInfo = stateInfo;
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
		return stateInfo.getSystemId();
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractFractalsTask<?>))
			return false;
		AbstractFractalsTask<?> other = (AbstractFractalsTask<?>)obj;
		return (other.stateInfo.getTaskId() == stateInfo.getTaskId() && other.stateInfo.getSystemId().equals(stateInfo.getSystemId()) && other.jobId == jobId);
	}
}
