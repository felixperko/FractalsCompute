package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.task.TaskAssignedMessage;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

/**
 * basic round robin implementation
 *
 */
public class LocalTaskProvider implements TaskProvider {
	
	static CategoryLogger log = new CategoryLogger("taskprovider", new ColorContainer(0f, 1f, 1f));

	List<TaskManager> taskManagers = Collections.synchronizedList(new ArrayList<>());
	
	int roundRobinIndex = 0;
	
	List<CalculateFractalsThread> localThreads = new ArrayList<>();
	
	LocalTaskProviderAdapter adapter = new LocalTaskProviderAdapter(this);
	
	Map<ClientConnection, Integer> requestedTasks = new HashMap<>();
	Map<ClientConnection, List<FractalsTask>> assignedTasks = new HashMap<>();
	
//	Map<TaskManager, Long> threadTimeTaken = new HashMap<>();
	
	public void addTaskManager(TaskManager taskManager) {
		taskManagers.add(taskManager);
		taskManager.addTaskProviderAdapter(adapter);
//		threadTimeTaken.clear();
	}
	
	public void removeTaskManager(TaskManager taskManager) {
		taskManagers.remove(taskManager);
		taskManager.removeTaskProviderAdapter(adapter);
//		threadTimeTaken.clear();
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
	public FractalsTask getTask() {
		return getNextTask();
	}
	
	protected synchronized FractalsTask getNextTask() {
		
//		List<TaskManager> priorityList = new ArrayList<>();
//		List<Long> times = new ArrayList<>();
//		for (Entry<TaskManager, Long> e : threadTimeTaken.entrySet()) {
//			priorityList.add(e.getKey())
//		}
		
		for (int i = 0 ; i < taskManagers.size() ; i++) {
			if (roundRobinIndex >= taskManagers.size())
				roundRobinIndex = 0;
			TaskManager manager = taskManagers.get(roundRobinIndex++);
			List<? extends FractalsTask> tasks = manager.getTasks(1);
			if (tasks != null && tasks.size() == 1)
				return tasks.get(0);
		}
		return null;
	}
	
	@Override
	public void finishedTask(FractalsTask task) {
		TaskManager tm = task.getTaskManager();
		tm.taskFinished(task);
//		Long oldTime = threadTimeTaken.get(tm);
//		if (oldTime == null)
//			oldTime = 0L;
//		threadTimeTaken.put(tm, (long)(oldTime+task.getLastExecutionTime()));
	}

	@Override
	public void taskAvailable() {
		
		for (CalculateFractalsThread thread : localThreads) {
			if (thread.getLifeCycleState().equals(LifeCycleState.IDLE)) {
				thread.taskAvailable();
				return;
			}
		}
		
		ClientConnection connection = null;
		int left = 0;
		for (Entry<ClientConnection, Integer> e : requestedTasks.entrySet()) {
			if (e.getValue() > left) {
				connection = e.getKey();
				left = e.getValue();
			}
		}
		if (connection != null) {
			FractalsTask task = getNextTask();
			if (task == null)
				return;
			List<FractalsTask> list = new ArrayList<>();
			list.add(task);
			
			assignRemoteTasks(connection, list);
			
			if (left == 1) {
				requestedTasks.remove(connection);
			} else {
				requestedTasks.put(connection, left-1);
			}
		}
	}
	
	public void assignRemoteTasks(ClientConnection connection, List<FractalsTask> tasks) {
		getAssignedTaskList(connection).addAll(tasks);
		TaskAssignedMessage msg = new TaskAssignedMessage(tasks);
		connection.writeMessage(msg);
		
		Integer left = requestedTasks.get(connection);
		if (left == null) {
			log.log("warn", "Assigning Task that wasn't requested? (LocalTaskProvider)");
			left = 0;
		}
		left -= tasks.size();
		if (left <= 0)
			requestedTasks.remove(connection);
		else
			requestedTasks.put(connection, left);
	}
	
	public void completedRemoteTasks(ClientConnection connection, List<FractalsTask> tasks) {
		getAssignedTaskList(connection).removeAll(tasks);
		tasks.forEach(t -> finishedTask(t));
	}
	
	private List<FractalsTask> getAssignedTaskList(ClientConnection connection){
		List<FractalsTask> list = assignedTasks.get(connection);
		if (list == null) {
			list = new ArrayList();
			assignedTasks.put(connection, list);
		}
		return list;
	}

	@Override
	public void cancelTasks() {
		for (CalculateFractalsThread t : localThreads)
			t.abortTask();
	}

	
	public void setClientRequests(ClientConnection connection, int tasksLeft) {
		
	}

}
