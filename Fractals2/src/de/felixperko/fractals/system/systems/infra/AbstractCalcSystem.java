package de.felixperko.fractals.system.systems.infra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.manager.ServerThreadManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public abstract class AbstractCalcSystem implements CalcSystem {
	
	protected UUID id = UUID.randomUUID();
	
	LifeCycleState state = LifeCycleState.NOT_INITIALIZED;
	
	List<FractalsThread> threads = new ArrayList<>();
	
	protected ServerManagers managers;
	
	public AbstractCalcSystem(ServerManagers managers) {
		this.managers = managers;
	}
	
	@Override
	public void init(Map<String, ParamSupplier> parameters) {
		if (onInit(parameters))
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
	
	public abstract boolean onInit(Map<String, ParamSupplier> params);
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
	
	@Override
	public UUID getId() {
		return id;
	}
}
