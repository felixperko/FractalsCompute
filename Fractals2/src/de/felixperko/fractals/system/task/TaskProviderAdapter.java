package de.felixperko.fractals.system.task;

public interface TaskProviderAdapter {
	
	public void cancelTasks();
	
	public void taskAvailable(int count);
}
