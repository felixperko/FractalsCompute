package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;

public abstract class AbstractFractalsThread extends Thread implements FractalsThread{
	
	LifeCycleState state;
	protected ThreadManager threadManager;
	
	public AbstractFractalsThread(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}
	
	@Override
	public void continueThread() {
		interrupt();
	}

	@Override
	public void pauseThread() {
		setLifeCycleState(LifeCycleState.PAUSED);
		interrupt();
	}
	
	@Override
	public void stopThread() {
		setLifeCycleState(LifeCycleState.STOPPED);
		threadManager.removeThread(this);
		interrupt();
	}
	
	@Override
	public void setLifeCycleState(LifeCycleState state) {
		this.state = state;
	}
	
	@Override
	public LifeCycleState getLifeCycleState() {
		return state;
	}
}
