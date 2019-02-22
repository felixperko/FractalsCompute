package de.felixperko.fractals.system.task;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.felixperko.fractals.manager.client.ClientManagers;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.messages.task.TaskFinishedMessage;
import de.felixperko.fractals.network.messages.task.TaskRequestMessage;
import de.felixperko.fractals.network.messages.task.TaskStateChangedMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;

public class RemoteTaskProvider implements TaskProvider {
	
	int bufferSize;
	
	Queue<FractalsTask> bufferedTasks = new LinkedList<>();
	ClientManagers managers;
	
	public RemoteTaskProvider(ClientManagers managers, int bufferSize) {
		this.managers = managers;
		this.bufferSize = bufferSize;
	}
	
	private ServerConnection getServerConnection() {
		return managers.getNetworkManager().getServerConnection();
	}
	
	protected void requestTasks(int amount) {
		getServerConnection().writeMessage(new TaskRequestMessage(amount));
	}
	
	public void addTasks(List<FractalsTask> taskList) {
		bufferedTasks.addAll(taskList);
	}

	@Override
	public FractalsTask getTask() {
		return bufferedTasks.poll();
	}
	
	public void taskStateChanged(FractalsTask task) {
		getServerConnection().writeMessage(new TaskStateChangedMessage(task.getId(), task.getState()));
	}

	@Override
	public void finishedTask(FractalsTask task) {
		getServerConnection().writeMessage(new TaskFinishedMessage(task));
	}

}
