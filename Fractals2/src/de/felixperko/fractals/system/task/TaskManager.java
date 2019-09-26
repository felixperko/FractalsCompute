package de.felixperko.fractals.system.task;

import java.util.List;

import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public interface TaskManager<T> {
	void startTasks();
	void endTasks();
	boolean setParameters(ParamContainer paramContainer);
	void reset();
	void taskFinished(FractalsTask task);
	List<? extends FractalsTask> getTasks(int count);
	CalcSystem getSystem();
	
	public void addTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter);
	public void removeTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter);
}
