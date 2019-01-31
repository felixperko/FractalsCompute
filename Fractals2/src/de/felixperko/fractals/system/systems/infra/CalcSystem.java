package de.felixperko.fractals.system.systems.infra;

import java.util.HashMap;

public interface CalcSystem {
	public void init(HashMap<String, String> settings);
	public void start();
	public void pause();
	public void stop();
	public CalcSystemState getSystemState();
}

enum CalcSystemState{
	NOT_INITIALIZED, INITIALIZED, RUNNING, PAUSED, STOPPED;
}
