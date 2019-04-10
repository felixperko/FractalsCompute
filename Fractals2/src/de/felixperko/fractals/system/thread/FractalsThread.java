package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.system.systems.infra.LifeCycleComponent;

public interface FractalsThread extends LifeCycleComponent{

	void start();
	void continueThread();
	void pauseThread();
	void stopThread();
	
	public boolean isNotified();
	public void resetNotified();

}
