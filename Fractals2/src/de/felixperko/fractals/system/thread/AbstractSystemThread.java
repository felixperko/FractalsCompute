package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public abstract class AbstractSystemThread extends AbstractFractalsThread {
	protected CalcSystem system;

	public AbstractSystemThread(Managers managers, CalcSystem system) {
		super(managers);
		this.system = system;
	}
	
	public CalcSystem getSystem() {
		return system;
	}
}
