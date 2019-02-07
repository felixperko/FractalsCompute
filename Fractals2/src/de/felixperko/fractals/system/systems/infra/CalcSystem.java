package de.felixperko.fractals.system.systems.infra;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ClientConnection;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public interface CalcSystem extends LifeCycleComponent{
	public UUID getId();
	public void init(HashMap<String, String> settings);
	public void start();
	public void pause();
	public void stop();
	public void reset();
	public void addClient(ClientConfiguration newConfiguration, Map<String, ParamSupplier> parameters);
	public void changedClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration);
	public void removeClient(ClientConfiguration oldConfiguration);
	public void changeClientMaxThreadCount(int newGranted, int oldGranted);
	public boolean isApplicable(ClientConnection connection, Map<String, ParamSupplier> map);
}
