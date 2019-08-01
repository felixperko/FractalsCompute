package de.felixperko.fractals.system.systems.stateinfo;

import de.felixperko.fractals.system.task.Layer;

public class TaskStateInfo{
	
	int taskId;
	TaskState state;
	double progress;
	transient SystemStateInfo systemStateInfo;
	Layer layer;
	
	transient TaskStateUpdate updateMessage;
	
	public TaskStateInfo(int taskId, Layer layer) {
		this.taskId = taskId;
		this.state = TaskState.PLANNED;
		this.progress = 0;
		this.layer = layer;
	}
	
	public TaskStateInfo() {
		this.taskId = -1;
		this.state = TaskState.PLANNED;
		this.progress = 0;
		this.layer = null;
	}

	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		if (this.state == state)
			return;
		TaskState oldState = this.state;
		this.state = state;
		
		updateMessage(state, oldState);
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
		updateMessage(state);
	}
	
	private void updateMessage(TaskState state) {
		updateMessage(state, this.state);
	}

	private void updateMessage(TaskState state, TaskState oldState) {
		if (updateMessage == null || updateMessage.isSent())
			updateMessage = systemStateInfo.taskStateChanged(taskId, oldState, this);
		else {
			synchronized (updateMessage) {
				updateMessage.refresh(state, layer.getId(), progress);
				systemStateInfo.taskStateUpdated(updateMessage);
			}
		}
	}

	public int getTaskId() {
		return taskId;
	}

	protected void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public SystemStateInfo getSystemStateInfo() {
		return systemStateInfo;
	}

	protected void setSystemStateInfo(SystemStateInfo systemStateInfo) {
		this.systemStateInfo = systemStateInfo;
	}

	
	public Layer getLayer() {
		return layer;
	}
	

	public void setLayer(Layer layer) {
		this.layer = layer;
	}

}