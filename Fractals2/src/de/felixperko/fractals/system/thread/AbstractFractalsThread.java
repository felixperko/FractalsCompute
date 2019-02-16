package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.manager.ServerThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;

public abstract class AbstractFractalsThread extends Thread implements FractalsThread{
	
	LifeCycleState state;
	protected Managers managers;
	
	public AbstractFractalsThread(Managers managers) {
		this.managers = managers;
		managers.getThreadManager().addThread(this);
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
		managers.getThreadManager().removeThread(this);
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
