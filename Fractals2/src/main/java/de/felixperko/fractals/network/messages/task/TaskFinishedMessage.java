package de.felixperko.fractals.network.messages.task;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.system.task.FractalsTask;

public class TaskFinishedMessage extends ClientMessage {

	private static final long serialVersionUID = -5817288498408381961L;
	
	List<FractalsTask> tasks;

	public TaskFinishedMessage(FractalsTask task) {
		tasks = new ArrayList<>();
		tasks.add(task);
	}
	
	public TaskFinishedMessage(List<FractalsTask> tasks) {
		this.tasks = tasks;
	}

	@Override
	protected void process() {
		getReceiverManagers().getThreadManager().getTaskProvider().completedRemoteTasks(getBackConnection(), tasks);
	}

}
