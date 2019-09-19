package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public class OrbitSystem extends AbstractCalcSystem {
	
	OrbitTaskManager taskManager;

	public OrbitSystem(ServerManagers managers) {
		super(managers);
		this.taskManager = new OrbitTaskManager(managers, this);
	}

	@Override
	public void reset() {
		this.taskManager.reset();
	}

	@Override
	public void changeClientMaxThreadCount(int newGranted, int oldGranted) {
		
	}

	@Override
	public ParameterConfiguration createParameterConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onInit(ParamContainer paramContainer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onStart() {
		return true;
	}

	@Override
	public boolean onPause() {
		return true;
	}

	@Override
	public boolean onStop() {
		return true;
	}

	@Override
	public void addedClient(ClientConfiguration newConfiguration, SystemClientData systemClientData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changedClient(ParamContainer paramContainer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removedClient(ClientConfiguration oldConfiguration) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public SystemContext getContext() {
		if (taskManager == null)
			return null;
		return taskManager.context;
	}

}
