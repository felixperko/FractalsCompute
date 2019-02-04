package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public abstract class AbstractSystemThread extends AbstractFractalsThread {
	protected CalcSystem system;

	public AbstractSystemThread(ThreadManager threadManager, CalcSystem system) {
		super(threadManager);
		this.system = system;
	}
	
	public CalcSystem getSystem() {
		return system;
	}
}
