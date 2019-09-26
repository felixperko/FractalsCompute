package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.task.TaskAssignedMessage;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

/**
 * basic round robin implementation
 *
 */
public class LocalTaskProvider implements TaskProvider {
	
	static CategoryLogger log = new CategoryLogger("taskprovider", new ColorContainer(0f, 1f, 1f));
	
	ServerManagers managers;

	List<TaskManager<?>> taskManagers = Collections.synchronizedList(new ArrayList<>());
	
	int roundRobinIndex = 0;
	
	List<CalculateFractalsThread> localThreads = new ArrayList<>();
	
	LocalTaskProviderAdapter adapter = new LocalTaskProviderAdapter(this);
	
	Map<ClientConnection, Integer> requestedTasks = new HashMap<>();
	Map<ClientConnection, List<FractalsTask>> assignedTasks = new HashMap<>();
	
//	Map<TaskManager, Long> threadTimeTaken = new HashMap<>();
	
	public LocalTaskProvider(ServerManagers managers) {
		this.managers = managers;
	}

	public synchronized void addTaskManager(TaskManager<?> taskManager) {
		taskManagers.add(taskManager);
		taskManager.addTaskProviderAdapter(adapter);
//		threadTimeTaken.clear();
	}
	
	public synchronized void removeTaskManager(TaskManager<?> taskManager) {
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
			TaskManager<?> manager = taskManagers.get(roundRobinIndex++);
			List<? extends FractalsTask> tasks = manager.getTasks(1);
			if (tasks != null && tasks.size() == 1)
				return tasks.get(0);
		}
		return null;
	}
	
	@Override
	public void finishedTask(FractalsTask task) {
		TaskManager tm = task.getTaskManager();
		if (tm != null){
			tm.taskFinished(task);
			return;
		} else {
			for (TaskManager<?> tm2 : taskManagers){
				if (tm2.getSystem().getId().equals(task.getSystemId())){
					tm2.taskFinished(task);
					return;
				}
			}
		}
		throw new IllegalStateException("cant find task manager for task");
//		Long oldTime = threadTimeTaken.get(tm);
//		if (oldTime == null)
//			oldTime = 0L;
//		threadTimeTaken.put(tm, (long)(oldTime+task.getLastExecutionTime()));
	}

	@Override
	public void taskAvailable() {
		
		//try to find local idle thread to notify
		for (CalculateFractalsThread thread : localThreads) {
			if (thread.getLifeCycleState().equals(LifeCycleState.IDLE)) {
				thread.taskAvailable();
				return;
			}
		}
		
		//distribute to remote if not locally assigned
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
	
	public void assignRemoteTasks(ClientConnection connection, int amount) {
		
		//updated requested count
		Integer left = requestedTasks.get(connection);
		if (left == null) {
			left = 0;
		}
		if (amount > left)
			requestedTasks.put(connection, (Integer)amount);
		
		//get tasks
		List<FractalsTask> tasks = new ArrayList<>();
		for (int i = 0 ; i < left ; i++) {
			FractalsTask task = getTask();
			if (task == null)
				break;
			tasks.add(task);
		}
		
		assignRemoteTasks(connection, tasks);
		
	}
	
	protected void assignRemoteTasks(ClientConnection connection, List<FractalsTask> tasks) {
		if (tasks.isEmpty())
			return;
		
		//assign tasks
		for (FractalsTask task : tasks){
			task.getStateInfo().setState(TaskState.ASSIGNED);
			managers.getSystemManager().addTask(task);
		}
		getAssignedTaskList(connection).addAll(tasks);
		
		//send message
		TaskAssignedMessage msg = new TaskAssignedMessage(tasks);
		connection.writeMessage(msg);
		
		//update requested tasks count
		Integer left = requestedTasks.get(connection);
		left -= tasks.size();
		if (left <= 0)
			requestedTasks.remove(connection);
		else
			requestedTasks.put(connection, left);
	}
	
	public void completedRemoteTasks(ClientConnection connection, List<FractalsTask> tasks) {
		List<FractalsTask> localList = getAssignedTaskList(connection);
		for (FractalsTask localTask : localList){
			for (FractalsTask remoteTask : tasks){
				if (remoteTask.equals(localTask)){
					remoteTask.applyLocalState(localTask);
					localTask.getTaskManager().taskFinished(remoteTask);
					managers.getSystemManager().removeTask(localTask);
				}
			}
		}
		getAssignedTaskList(connection).removeAll(tasks);
		tasks.forEach(t -> finishedTask(t));
	}
	
	private List<FractalsTask> getAssignedTaskList(ClientConnection connection){
		List<FractalsTask> list = assignedTasks.get(connection);
		if (list == null) {
			list = new ArrayList<>();
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
