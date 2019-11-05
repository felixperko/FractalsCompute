package de.felixperko.fractals.system.systems.OrbitSystem;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.systems.common.BFOrbitCommon;
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
	public ParameterConfiguration createParameterConfiguration() {
		//iterations, calculator, midpoint, numberFactory, chunkFactory, systemName, view, calculator specifics
		ParameterConfiguration config = BFOrbitCommon.getCommonParameterConfiguration();
		return config;
	}

	@Override
	public boolean onInit(ParamContainer paramContainer) {
		taskManager = new OrbitTaskManager(managers, this);
		taskManager.setParameters(paramContainer);
		return true;
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
	public void addedClient(ClientConfiguration newConfiguration, ParamContainer paramContainer) {
		taskManager.setParameters(paramContainer);
	}

	@Override
	public void changedClient(ParamContainer paramContainer) {
		taskManager.setParameters(paramContainer);
	}

	@Override
	public void removedClient(ClientConfiguration oldConfiguration) {
	}

	
	@Override
	public SystemContext getContext() {
		if (taskManager == null)
			return null;
		return taskManager.context;
	}

}
