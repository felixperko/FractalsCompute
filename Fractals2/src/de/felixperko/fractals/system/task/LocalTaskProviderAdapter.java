package de.felixperko.fractals.system.task;

public class LocalTaskProviderAdapter implements TaskProviderAdapter {
	
	LocalTaskProvider taskProvider;
	
	public LocalTaskProviderAdapter(LocalTaskProvider localTaskProvider) {
		this.taskProvider = localTaskProvider;
	}

	@Override
	public void cancelTasks() {
		taskProvider.cancelTasks();
	}

	@Override
	public void taskAvailable(int count) {
		for (int i = 0 ; i < count ; i++)
			taskProvider.taskAvailable();
	}
}
