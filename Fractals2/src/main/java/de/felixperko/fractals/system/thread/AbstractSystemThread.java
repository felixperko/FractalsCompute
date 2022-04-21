package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.system.systems.infra.CalcSystem;

public abstract class AbstractSystemThread extends AbstractFractalsThread {
	protected CalcSystem system;

	public AbstractSystemThread(ServerManagers managers, CalcSystem system, String name) {
		super(managers, name);
		this.system = system;
	}
	
	public CalcSystem getSystem() {
		return system;
	}
}
