package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * basic round robin implementation
 *
 */
public class LocalTaskProvider implements TaskProvider {

	List<TaskManager> taskManagers = Collections.synchronizedList(new ArrayList<>());
	
	int roundRobinIndex = 0;
	
//	Map<TaskManager, Long> threadTimeTaken = new HashMap<>();
	
	public void addTaskManager(TaskManager taskManager) {
		taskManagers.add(taskManager);
//		threadTimeTaken.clear();
	}
	
	public void removeTaskManager(TaskManager taskManager) {
		taskManagers.remove(taskManager);
//		threadTimeTaken.clear();
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
			List<FractalsTask> tasks = manager.getTasks(1);
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

}
