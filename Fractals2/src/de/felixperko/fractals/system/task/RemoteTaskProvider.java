package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.messages.task.TaskFinishedMessage;
import de.felixperko.fractals.network.messages.task.TaskRequestMessage;
import de.felixperko.fractals.network.messages.task.TaskStateChangedMessage;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public class RemoteTaskProvider extends Thread implements TaskProvider {
	
	int bufferSize;
	
	Queue<FractalsTask> bufferedTasks = new LinkedList<>();
	List<ServerConnection> serverConnections = new ArrayList<>();
	List<CalculateFractalsThread> localThreads = new ArrayList<>();
	
	Map<FractalsTask, ServerConnection> taskConnectionMap = new HashMap<>();
	
	public RemoteTaskProvider(int bufferSize) {
		this.bufferSize = bufferSize;
		this.start();
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			if (bufferedTasks.size() < bufferSize)
				requestTasks(bufferSize-bufferedTasks.size());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addServerConnection(ServerConnection serverConnection) {
		serverConnections.add(serverConnection);
	}
	
	public void requestTasks(int amount) {
		
		int connCount = serverConnections.size();
		//TODO client at multiple connections -> balance requests
		for (ServerConnection serverConnection : serverConnections) {
			int amount2 = amount/connCount;
			amount -= amount2;
			connCount--;
			serverConnection.writeMessage(new TaskRequestMessage(amount2));
		}
	}
	
	public void addTasks(List<FractalsTask> taskList, ServerConnection serverConnection) {
		for (FractalsTask task : taskList)
			bufferedTasks.add(task);
		for (FractalsTask task : taskList)
			taskConnectionMap.put(task, serverConnection);
	}

	@Override
	public FractalsTask getTask() {
		FractalsTask task = bufferedTasks.poll();
		return task;
	}
	
	public void taskStateChanged(FractalsTask task) {
		ServerConnection serverConnection = taskConnectionMap.get(task);
		if (serverConnection != null)
			serverConnection.writeMessage(new TaskStateChangedMessage(task.getSystemId(), task.getId(), task.getState()));
		else
			throw new IllegalStateException("Can't find connection for task");
	}

	@Override
	public void finishedTask(FractalsTask task) {
		ServerConnection serverConnection = taskConnectionMap.get(task);
		if (serverConnection != null)
			serverConnection.writeMessage(new TaskFinishedMessage(task));
		else
			throw new IllegalStateException("Can't find connection for task");
	}

	@Override
	public void taskAvailable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLocalCalculateThread(CalculateFractalsThread calculateFractalsThread) {
		localThreads.add(calculateFractalsThread);
	}

	@Override
	public void removeLocalCalculateThread(CalculateFractalsThread calculateFractalsThread) {
		localThreads.remove(calculateFractalsThread);
	}

	@Override
	public void cancelTasks() {
		for (CalculateFractalsThread t : localThreads)
			t.abortTask();
	}

}
