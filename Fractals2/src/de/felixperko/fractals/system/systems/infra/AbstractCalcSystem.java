package de.felixperko.fractals.system.systems.infra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public abstract class AbstractCalcSystem implements CalcSystem {
	
	LifeCycleState state = LifeCycleState.NOT_INITIALIZED;
	
	List<FractalsThread> threads = new ArrayList<>();
	
	TaskManager taskManager;
	
	protected ThreadManager threadManager;
	
	public AbstractCalcSystem(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}
	
	@Override
	public void init(HashMap<String, String> settings) {
		if (onInit(settings))
			state = LifeCycleState.INITIALIZED;
	}

	@Override
	public void start() {
		if (onStart()) {
			state = LifeCycleState.RUNNING;
		}
	}

	@Override
	public void pause() {
		if (onPause()) {
			state = LifeCycleState.PAUSED;
		}
	}

	@Override
	public void stop() {
		if (onStop()) {
			state = LifeCycleState.STOPPED;
			for (FractalsThread thread : threads)
				thread.stopThread();
		}
	}
	
	public void addThread(FractalsThread thread) {
		threads.add(thread);
	}
	
	public abstract boolean onInit(HashMap<String, String> settings);
	public abstract boolean onStart();
	public abstract boolean onPause();
	public abstract boolean onStop();
	
	@Override
	public LifeCycleState getLifeCycleState() {
		return state;
	}
	
	@Override
	public void setLifeCycleState(LifeCycleState state) {
		this.state = state;
	}
}
