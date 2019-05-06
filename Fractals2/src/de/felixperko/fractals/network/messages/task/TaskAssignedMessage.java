package de.felixperko.fractals.network.messages.task;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.network.infra.ServerMessage;
import de.felixperko.fractals.system.task.FractalsTask;

public class TaskAssignedMessage extends ServerMessage {
	
	List<FractalsTask> tasks = new ArrayList<>();
	
	@Override
	protected void process() {
		getBackConnection().getNetworkManager().getMessageInterface().assignedTasks(tasks);
	}

}
