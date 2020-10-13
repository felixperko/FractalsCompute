package de.felixperko.fractals.system.systems.infra;

import java.util.List;
import java.util.UUID;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;

public interface CalcSystem extends LifeCycleComponent{
	public UUID getId();
	
	public void init(ParamContainer paramContainer);
	public void start();
	public void pause();
	public void stop();
	public void reset();
	
	public void addClient(ClientConfiguration newConfiguration, ParamContainer paramContainer);
	public void changeClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration);
	public void removeClient(ClientConfiguration oldConfiguration);
	public List<ClientConfiguration> getClients();

	public ParamConfiguration getParameterConfiguration();
	public ParamConfiguration createParameterConfiguration();
	public boolean isApplicable(ClientConnection connection, ParamContainer paramContainer);
	
	public SystemStateInfo getSystemStateInfo();

	public SystemContext getContext();
}
