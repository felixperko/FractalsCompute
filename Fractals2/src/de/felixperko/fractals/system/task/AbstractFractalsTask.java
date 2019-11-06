package de.felixperko.fractals.system.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.statistics.EmptyStats;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;

public abstract class AbstractFractalsTask<T> implements FractalsTask{
	
	private static final long serialVersionUID = -3755610537350804691L;
	
	private ParamContainer context_params; //to serialize parameters without the whole context
	transient SystemContext<?> context;
	transient TaskManager<T> taskManager;
	
	int jobId;
	
	TaskStateInfo stateInfo;
	
	protected IStats taskStats = new EmptyStats();
	
	boolean cancelled;
	
	public AbstractFractalsTask(SystemContext<?> context, Integer id, TaskManager<T> taskManager, int jobId, Layer layer) {
		this.jobId = jobId;
		System.out.println("Created Task with jobid "+jobId);
		this.taskManager = taskManager;
		this.context = context;
		this.stateInfo = new TaskStateInfo(id, taskManager.getSystem().getId(), context);
		stateInfo.setLayer(layer);
		taskManager.getSystem().getSystemStateInfo().addTaskStateInfo(stateInfo);
	}

	@Override
	public SystemContext getContext() {
		return context;
	}
	
	public ParamContainer getContextParams(boolean beforeSerialization) {
		if (beforeSerialization && context != null)
			return context.getParamContainer();
		return context_params;
	}
	
	public void setContext(SystemContext<?> context) {
		this.context = context;
		stateInfo.setContext(context);
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

	public IStats getTaskStats() {
		return taskStats;
	}

	public void setTaskStats(IStats taskStats) {
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
	
	@Override
	public void applyLocalState(FractalsTask localTask) {
		setContext(localTask.getContext());
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException{
		if (context != null)
			context_params = context.getParamContainer();
		oos.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException{
		ois.defaultReadObject();
	}
}
