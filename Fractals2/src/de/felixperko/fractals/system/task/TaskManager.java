package de.felixperko.fractals.system.task;

import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public interface TaskManager<T> {
	void startTasks();
	void endTasks();
	boolean setParameters(Map<String, ParamSupplier> params);
	void reset();
	void taskFinished(FractalsTask task);
	List<? extends FractalsTask> getTasks(int count);
	CalcSystem getSystem();
	
	public void addTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter);
	public void removeTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter);
}
