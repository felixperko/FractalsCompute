package de.felixperko.fractals.system.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTask;

public interface TaskManager<T> {
	void startTasks();
	void endTasks();
	boolean setParameters(Map<String, ParamSupplier> params);
	void reset();
	void taskFinished(T task);
	List<? extends FractalsTask> getTasks(int count);
}
