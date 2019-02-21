package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;

public class TaskStateInfo implements Serializable{
	
	private static final long serialVersionUID = -5333555292596681679L;
	
	int taskId;
	TaskState state;
	double progress;
	int stage;
	SystemStateInfo systemStateInfo;
	
	public TaskStateInfo(int taskId) {
		this.taskId = taskId;
		this.state = TaskState.PLANNED;
		this.progress = 0;
		this.stage = 0;
	}
	
	public TaskStateInfo() {
		this.taskId = -1;
		this.state = TaskState.PLANNED;
		this.progress = 0;
		this.stage = 0;
	}

	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		if (this.state == state)
			return;
		
		systemStateInfo.getTaskListForState(this.state).remove(this);
		
		this.state = state;
		
		systemStateInfo.getTaskListForState(state).add(this);
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
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
}