package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.UUID;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public class OrbitSystem extends AbstractCalcSystem {
	
	OrbitTaskManager taskManager;

	public OrbitSystem(UUID systemId, ServerManagers managers) {
		super(systemId, managers);
		this.taskManager = new OrbitTaskManager(managers, this);
	}

	@Override
	public void reset() {
		this.taskManager.reset();
	}

	@Override
	public ParamConfiguration createParameterConfiguration() {
		//iterations, calculator, midpoint, numberFactory, chunkFactory, systemName, view, calculator specifics
		ParamConfiguration config = CommonFractalParameters.getCommonParameterConfiguration();
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
