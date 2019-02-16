package de.felixperko.fractals.system.task;

public interface TaskProvider {
	public FractalsTask getTask();

	public void finishedTask(FractalsTask currentTask);
	
	public void addTaskManager(TaskManager taskManager);
	public void removeTaskManager(TaskManager taskManager);
}
