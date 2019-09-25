package de.felixperko.fractals.system.systems.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.SystemConnectedMessage;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;
import de.felixperko.fractals.system.thread.FractalsThread;

public abstract class AbstractCalcSystem implements CalcSystem {
	
	static int CALC_SYSTEM_COUNTER = 0;
	
	protected UUID id = UUID.randomUUID();
	int number = 0;
	
	List<ClientConfiguration> clients = new ArrayList<>();
	LifeCycleState state = LifeCycleState.NOT_INITIALIZED;
	
	List<FractalsThread> threads = new ArrayList<>();
	
	protected ServerManagers managers;
	
	SystemStateInfo systemStateInfo;
	
	ParameterConfiguration parameterConfiguration;
	
	public AbstractCalcSystem(ServerManagers managers) {
		this.managers = managers;
		this.number = CALC_SYSTEM_COUNTER++;
		this.systemStateInfo = new SystemStateInfo(id);
		managers.getSystemManager().getStateInfo().addSystemStateInfo(id, this.systemStateInfo);
		this.parameterConfiguration = createParameterConfiguration();
	}
	
	@Override
	public void init(ParamContainer paramContainer) {
		if (onInit(paramContainer))
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
		LifeCycleState oldState = state;
		state = LifeCycleState.STOPPED;
		if (onStop()) {
			for (FractalsThread thread : threads)
				thread.stopThread();
		} else {
			state = oldState;
		}
	}
	
	public void addThread(FractalsThread thread) {
		threads.add(thread);
	}
	
	public abstract boolean onInit(ParamContainer paramContainer);
	public abstract boolean onStart();
	public abstract boolean onPause();
	public abstract boolean onStop();
	
	@Override
	public void addClient(ClientConfiguration newConfiguration, ParamContainer paramContainer) {
		synchronized (clients) {
			clients.add(newConfiguration);
			newConfiguration.getSystemRequests().remove(paramContainer);
			newConfiguration.getParamContainers().put(id, paramContainer);
			newConfiguration.getConnection().writeMessage(new SystemConnectedMessage(id, newConfiguration, getParameterConfiguration()));
			addedClient(newConfiguration, paramContainer);
		}
	}
	
	@Override
	public void changeClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration) {
		
		
		ParamContainer newParameters = newConfiguration.getParamContainer(getId());
		
		boolean applicable = isApplicable(newConfiguration.getConnection(), newParameters);
		synchronized(clients) {
			if (oldConfiguration != null)
				clients.remove(oldConfiguration);
			if (applicable) {
				clients.add(newConfiguration);
			}
			changedClient(newParameters);
		}
	}
	
	@Override
	public void removeClient(ClientConfiguration oldConfiguration) {
		synchronized(clients) {
			clients.remove(oldConfiguration);
			if (clients.isEmpty())
				stop();
			oldConfiguration.getConnection().setClosed();
			removedClient(oldConfiguration);
		}
	}
	
	public abstract void addedClient(ClientConfiguration newConfiguration, ParamContainer paramContainer);
	public abstract void changedClient(ParamContainer paramContainer);
	public abstract void removedClient(ClientConfiguration oldConfiguration);
	
	@Override
	public boolean isApplicable(ClientConnection connection, ParamContainer paramContainer) {
		boolean hasClient = false;
		for (ClientConfiguration conf : clients) {
			if (conf.getConnection() == connection) {
				hasClient = true;
				break;
			}
		}
		if (hasClient && clients.size() == 1)
			return true;
		for (ParamSupplier param : paramContainer.getClientParameters().values()) {
			if (param.isSystemRelevant() || param.isLayerRelevant()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public List<ClientConfiguration> getClients() {
		return clients;
	}
	
	@Override
	public ParameterConfiguration getParameterConfiguration() {
		return parameterConfiguration;
	}
	
	@Override
	public LifeCycleState getLifeCycleState() {
		return state;
	}
	
	@Override
	public void setLifeCycleState(LifeCycleState state) {
		this.state = state;
	}
	
	public SystemStateInfo getSystemStateInfo() {
		return systemStateInfo;
	}

	@Override
	public UUID getId() {
		return id;
	}
	
	public int getNumber(){
		return number;
	}
}
