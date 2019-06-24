package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;

import de.felixperko.fractals.system.task.Layer;

public class TaskStateInfo implements Serializable{
	
	private static final long serialVersionUID = -5333555292596681679L;
	
	int taskId;
	TaskState state;
	double progress;
	transient SystemStateInfo systemStateInfo;
	Layer layer;
	
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
		
		systemStateInfo.taskStateChanged(taskId, oldState, this);
		
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
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