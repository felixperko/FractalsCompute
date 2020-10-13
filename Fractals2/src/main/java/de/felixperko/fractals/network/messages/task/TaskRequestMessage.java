package de.felixperko.fractals.network.messages.task;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.system.task.LocalTaskProvider;

public class TaskRequestMessage extends ClientMessage {
	
	private static final long serialVersionUID = 4764129702185799026L;
	
	int amount;
	float cpuTasksPriority, gpuTasksPriority;

	public TaskRequestMessage(int amount, float cpuTasksPriority, float gpuTasksPriority) {
		this.amount = amount;
		this.cpuTasksPriority = cpuTasksPriority;
		this.gpuTasksPriority = gpuTasksPriority;
	}
	
	@Override
	protected void process() {
		ServerManagers managers = getReceiverManagers();
		ServerThreadManager threadManager = managers.getThreadManager();
		LocalTaskProvider taskProvider = threadManager.getTaskProvider();
		
		taskProvider.assignRemoteTasks(getBackConnection(), amount, cpuTasksPriority, gpuTasksPriority);
	}

}
