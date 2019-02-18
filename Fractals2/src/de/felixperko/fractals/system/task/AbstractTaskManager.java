package de.felixperko.fractals.system.task;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;

public abstract class AbstractTaskManager<T> extends AbstractFractalsThread implements TaskManager<T> {
	
	static int ID_COUNTER = 0;
	
	protected int taskManagerId;
	
	protected CalcSystem system;
	
	public AbstractTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, "TM_"+ID_COUNTER);
		taskManagerId = ID_COUNTER++;
		this.system = system;
	}

}
