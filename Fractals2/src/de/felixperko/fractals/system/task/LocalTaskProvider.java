package de.felixperko.fractals.system.task;

import java.util.List;

public class LocalTaskProvider implements TaskProvider {

	TaskManager taskManager;
	
	public LocalTaskProvider(TaskManager taskManager) {
		this.taskManager = taskManager;
	}
	
	@Override
	public FractalsTask getTask() {
		List<FractalsTask> taskList = taskManager.getTasks(1);
		if (taskList == null)
			return null;
		return taskList.get(0);
	}

	
	@Override
	public void finishedTask(FractalsTask task) {
		taskManager.taskFinished(task);
	}

}
