package de.felixperko.fractals.system.systems.infra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public abstract class AbstractCalcSystem implements CalcSystem {
	
	CalcSystemState state = CalcSystemState.NOT_INITIALIZED;
	
	List<FractalsThread> threads = new ArrayList<>();
	
	TaskManager taskManager;
	
	@Override
	public void init(HashMap<String, String> settings) {
		if (onInit(settings))
			state = CalcSystemState.INITIALIZED;
	}

	@Override
	public void start() {
		if (onStart()) {
			state = CalcSystemState.RUNNING;
		}
	}

	@Override
	public void pause() {
		if (onPause()) {
			state = CalcSystemState.PAUSED;
		}
	}

	@Override
	public void stop() {
		if (onStop()) {
			state = CalcSystemState.STOPPED;
			for (FractalsThread thread : threads)
				thread.stopThread();
		}
	}

	@Override
	public CalcSystemState getSystemState() {
		return state;
	}
	
	public void addThread(FractalsThread thread) {
		threads.add(thread);
	}
	
	public abstract boolean onInit(HashMap<String, String> settings);
	public abstract boolean onStart();
	public abstract boolean onPause();
	public abstract boolean onStop();

}
