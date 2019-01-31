package de.felixperko.fractals.system.task;

public interface TaskProvider {
	public FractalsTask getTask();

	public void finishedTask(FractalsTask currentTask);
}
