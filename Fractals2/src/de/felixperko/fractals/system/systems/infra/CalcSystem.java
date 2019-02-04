package de.felixperko.fractals.system.systems.infra;

import java.util.HashMap;

public interface CalcSystem extends LifeCycleComponent{
	public void init(HashMap<String, String> settings);
	public void start();
	public void pause();
	public void stop();
}
