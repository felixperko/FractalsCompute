package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.UUID;

import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.task.Layer;

public class TaskStateInfo implements Serializable{
	
	private static final long serialVersionUID = 4129352632333138169L;
	
	SystemContext systemContext;
	private transient TaskStateUpdate updateMessage;
	
	int taskId;
	UUID systemId;
	
	TaskState state;
	int layerId;
	double progress;
	
	public TaskStateInfo(int taskId, UUID systemId, SystemContext context) {
		this.taskId = taskId;
		this.systemId = systemId;
		this.systemContext = context;
		this.state = TaskState.PLANNED;
		this.progress = 0;
	}
	
//	public TaskStateInfo() {
//		this.taskId = -1;
//		this.state = TaskState.PLANNED;
//		this.progress = 0;
//		this.layerId = -1;
//	}

	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		if (this.state == state)
			return;
		TaskState oldState = this.state;
		this.state = state;
		
		updateMessage(oldState);
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
		updateMessage(state);
	}
	
	private void updateMessage(TaskState oldState) {
		if (systemContext != null)
			systemContext.taskStateUpdated(this, oldState);
	}

	public int getTaskId() {
		return taskId;
	}

	protected void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	public Layer getLayer() {
		return systemContext.getLayer(layerId);
	}
	
	public void setLayer(Layer layer) {
		this.layerId = layer.getId();
	}

	public TaskStateUpdate getUpdateMessage() {
		return updateMessage;
	}

	public void setUpdateMessage(TaskStateUpdate updateMessage) {
		this.updateMessage = updateMessage;
	}

	public int getLayerId() {
		return layerId;
	}

	public UUID getSystemId() {
		return systemId;
	}
	
	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
	
	public void setContext(SystemContext systemContext) {
		this.systemContext = systemContext;
	}

}