package de.felixperko.fractals.system.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.infra.LifeCycleStateChange;

public abstract class AbstractFractalsThread extends Thread implements FractalsThread{
	
	LifeCycleState state;
	protected Managers managers;
	
	static boolean LOG_LIFECYCLE_STATE = true;
	
	List<LifeCycleStateChange> lifeCycleHistory = new ArrayList<>();
	
	Integer lock = 0; //workaround to lock life cycle history
	
	public AbstractFractalsThread(Managers managers, String name) {
		this.managers = managers;
		managers.getThreadManager().addThread(this);
		setName(name);
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
		if (this.state == state)
			return;
		synchronized (lock) {
			lifeCycleHistory.add(new LifeCycleStateChange(this.state, state));
		}
		this.state = state;
	}
	
	@Override
	public LifeCycleState getLifeCycleState() {
		return state;
	}
	
	public List<LifeCycleStateChange> getLifeCycleHistory(boolean reset){
		synchronized (lock) {
			List<LifeCycleStateChange> res;
			if (reset) {
				res = lifeCycleHistory;
				lifeCycleHistory = new ArrayList<>();
			} else {
				res = new ArrayList<LifeCycleStateChange>(lifeCycleHistory);
			}
			return res;
		}
	}
}
