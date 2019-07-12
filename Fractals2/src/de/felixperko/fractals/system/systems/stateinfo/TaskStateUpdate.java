package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.UUID;

public class TaskStateUpdate extends AbstractSharedDataUpdate implements Serializable {
	
	private static final long serialVersionUID = -6210204399822115080L;
	
	UUID systemId;
	int taskId;
	int layerId;
	TaskState taskState;
	double progress;
	
	public TaskStateUpdate() {
		super();
	}

	public TaskStateUpdate(UUID systemId, int taskId, TaskState taskState, int layerId, double progress) {
		super();
		this.systemId = systemId;
		this.taskId = taskId;
		this.taskState = taskState;
		this.layerId = layerId;
		this.progress = progress;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public int getTaskId() {
		return taskId;
	}

	public int getLayerId() {
		return layerId;
	}

	public double getProgress() {
		return progress;
	}

	
	public void refresh(TaskState taskState, int layerId, double progress) {
		this.taskState = taskState;
		this.layerId = layerId;
		this.progress = progress;
	}
}
