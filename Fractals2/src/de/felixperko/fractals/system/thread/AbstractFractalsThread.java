package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.system.systems.infra.CalcSystem;

public abstract class AbstractFractalsThread extends Thread implements FractalsThread{
	
	protected CalcSystem system;
	
	protected boolean stopped = false;

	public AbstractFractalsThread(CalcSystem system) {
		this.system = system;
	}
	
	public CalcSystem getSystem() {
		return system;
	}
	
	@Override
	public void stopThread() {
		stopped = true;
		interrupt();
	}
}
