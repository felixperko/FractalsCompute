package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * basic round robin implementation
 *
 */
public class LocalTaskProvider implements TaskProvider {

	List<TaskManager> taskManagers = Collections.synchronizedList(new ArrayList<>());
	
	int roundRobinIndex = 0;
	
	@Override
	public void addTaskManager(TaskManager taskManager) {
		taskManagers.add(taskManager);
	}
	
	@Override
	public void removeTaskManager(TaskManager taskManager) {
		taskManagers.remove(taskManager);
	}
	
	@Override
	public FractalsTask getTask() {
		return getNextTask();
	}
	
	protected synchronized FractalsTask getNextTask() {
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
		task.getTaskManager().taskFinished(task);
	}

}
