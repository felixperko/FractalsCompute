package de.felixperko.fractals.network.messages.task;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.TaskProvider;

public class TaskRequestMessage extends ClientMessage {
	
	private static final long serialVersionUID = 4764129702185799026L;
	
	int amount;

	public TaskRequestMessage(int amount) {
		this.amount = amount;
	}
	
	@Override
	protected void process() {
		ServerManagers managers = getReceiverManagers();
		ServerThreadManager threadManager = managers.getThreadManager();
		LocalTaskProvider taskProvider = threadManager.getTaskProvider();
		
		List<FractalsTask> tasks = new ArrayList<>();
		for (int i = 0 ; i < amount ; i++) {
			FractalsTask task = taskProvider.getTask();
			if (task == null)
				break;
			tasks.add(task);
			task.getStateInfo().setState(TaskState.ASSIGNED);
		}
		
		if (!tasks.isEmpty()) {
			taskProvider.assignRemoteTasks(getBackConnection(), tasks);
		}
	}

}
