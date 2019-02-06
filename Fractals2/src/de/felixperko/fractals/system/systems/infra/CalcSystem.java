package de.felixperko.fractals.system.systems.infra;

import java.util.HashMap;
import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public interface CalcSystem extends LifeCycleComponent{
	public UUID getId();
	public void init(HashMap<String, String> settings);
	public void start();
	public void pause();
	public void stop();
	public void reset();
	public void addClient(ClientConfiguration newConfiguration);
	public void changedClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration);
	public void removeClient(ClientConfiguration oldConfiguration);
	public void changeClientMaxThreadCount(int newGranted, int oldGranted);
}
