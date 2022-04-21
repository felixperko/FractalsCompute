package de.felixperko.fractals.system.task;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;

public abstract class AbstractTaskManager<T> extends AbstractFractalsThread implements TaskManager<T> {
	
	static int ID_COUNTER = 0;
	
	protected int taskManagerId;
	
	protected CalcSystem system;
	
	protected List<TaskProviderAdapter> taskProviders = new ArrayList<>();
	
	public AbstractTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, "TM_"+ID_COUNTER);
		taskManagerId = ID_COUNTER++;
		this.system = system;
	}
	
	@Override
	public CalcSystem getSystem() {
		return system;
	}
	
	@Override
	public void addTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter) {
		taskProviders.add(taskProviderAdapter);
	}
	
	@Override
	public void removeTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter) {
		taskProviders.remove(taskProviderAdapter);
	}

}
