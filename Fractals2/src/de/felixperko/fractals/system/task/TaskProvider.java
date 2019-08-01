package de.felixperko.fractals.system.task;

import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public interface TaskProvider {
	public FractalsTask getTask();

	public void finishedTask(FractalsTask currentTask);
	
	public void taskAvailable();

	public void addLocalCalculateThread(CalculateFractalsThread calculateFractalsThread);
	public void removeLocalCalculateThread(CalculateFractalsThread calculateFractalsThread);
	
	public void cancelTasks();

	
//	public void addTaskManager(TaskManager taskManager);
//	public void removeTaskManager(TaskManager taskManager);
}
