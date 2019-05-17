package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

/**
 * basic round robin implementation
 *
 */
public class LocalTaskProvider implements TaskProvider {

	List<TaskManager> taskManagers = Collections.synchronizedList(new ArrayList<>());
	
	int roundRobinIndex = 0;
	
	List<CalculateFractalsThread> localThreads = new ArrayList<>();
	
	LocalTaskProviderAdapter adapter = new LocalTaskProviderAdapter(this);
	
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
	}

	@Override
	public void cancelTasks() {
		for (CalculateFractalsThread t : localThreads)
			t.abortTask();
	}

}
