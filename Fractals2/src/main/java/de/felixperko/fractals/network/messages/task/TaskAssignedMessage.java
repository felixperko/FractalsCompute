package de.felixperko.fractals.network.messages.task;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.network.infra.ServerMessage;
import de.felixperko.fractals.system.task.FractalsTask;

public class TaskAssignedMessage extends ServerMessage {
	
	private static final long serialVersionUID = 4803194986477065648L;
	
	List<FractalsTask> tasks = new ArrayList<>();
	
	public TaskAssignedMessage(List<FractalsTask> tasks) {
		this.tasks = tasks;
	}
	
	@Override
	protected void process() {
		getClientMessageInterface().assignedTasks(tasks);
	}

}
