package de.felixperko.fractals.system.systems.BreadthFirstSystem;


import static de.felixperko.fractals.system.systems.common.BFOrbitCommon.booleanType;
import static de.felixperko.fractals.system.systems.common.BFOrbitCommon.doubleType;
import static de.felixperko.fractals.system.systems.common.BFOrbitCommon.integerType;
import static de.felixperko.fractals.system.systems.common.BFOrbitCommon.listType;
import static de.felixperko.fractals.system.systems.common.BFOrbitCommon.numberType;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.ParamValueField;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.parameters.ParameterDefinition;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.common.BFOrbitCommon;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.task.ClassTaskFactory;
import de.felixperko.fractals.system.task.TaskFactory;

public class BreadthFirstSystem extends AbstractCalcSystem {
	
	private static final Logger LOG = LoggerFactory.getLogger(BreadthFirstSystem.class);
	
	TaskFactory factory_task = new ClassTaskFactory(BreadthFirstTask.class);
	
	BreadthFirstTaskManager taskManager;

	public BreadthFirstSystem(ServerManagers managers) {
		super(managers);
	}

	@Override
	public ParameterConfiguration createParameterConfiguration() {
		ParameterConfiguration config = BFOrbitCommon.getCommonParameterConfiguration();
		
		ParamValueType layerType = new ParamValueType("BreadthFirstLayer",
				new ParamValueField("priority_shift", doubleType, 0d),
				new ParamValueField("priority_multiplier", doubleType, 0d),
				new ParamValueField("samples", integerType, 1));
		ParamValueType upsampleLayerType = new ParamValueType("BreadthFirstUpsampleLayer", 
				new ParamValueField("priority_shift", doubleType, 0d),
				new ParamValueField("priority_multiplier", doubleType, 0d),
				new ParamValueField("upsample", integerType, 1),
				new ParamValueField("culling", booleanType, true));
		
		ParamValueType layerconfigurationType = new ParamValueType("LayerConfiguration",
				new ParamValueField("layers", listType),
				new ParamValueField("simStep", doubleType, 0.05),
				new ParamValueField("simCount", integerType, 20),
				new ParamValueField("seed", integerType, 42));
		
		config.addValueType(layerType);
		config.addValueType(upsampleLayerType);
		config.addValueType(layerconfigurationType);
		
		List<ParameterDefinition> defs_bf = new ArrayList<>();
		defs_bf.add(new ParameterDefinition("zoom", "Position", StaticParamSupplier.class, numberType)
				.withDescription("The current default coordinate zoom factor."));
		defs_bf.add(new ParameterDefinition("width", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The calculation width."));
		defs_bf.add(new ParameterDefinition("height", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The calculation height."));
		
		defs_bf.add(new ParameterDefinition("limit", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("Bailout radius. Increase to reduce coloring artifacts, Decrease to improve performance."));
		defs_bf.add(new ParameterDefinition("border_generation", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("The chunk distance from rendered area for which chunk calculation should continue."));
		defs_bf.add(new ParameterDefinition("border_dispose", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("The chunk distance at which chunks are deleted to preserve memory."));
		defs_bf.add(new ParameterDefinition("task_buffer", "Advanced", StaticParamSupplier.class, integerType)
				.withDescription("The amount of tasks that should be buffered for the calculation workers."));
		defs_bf.add(new ParameterDefinition("layerConfiguration", "Advanced", StaticParamSupplier.class, layerconfigurationType)
				.withDescription("Manages the layer order in which the calculation is performed."));
		config.addParameterDefinitions(defs_bf);
		
		config.addListTypes("layers", layerType, upsampleLayerType);
		return config;
	}

	@Override
	public boolean onInit(ParamContainer paramContainer) {
		
		LOG.info("initializing");
		taskManager = new BreadthFirstTaskManager(managers, this);
		taskManager.setParameters(paramContainer);
		
		return true;
	}

	@Override
	public boolean onStart() {

		LOG.info("starting");
		addThread(taskManager);
		taskManager.start();
		taskManager.startTasks();
		managers.getThreadManager().getTaskProvider().addTaskManager(taskManager);
		
		return true;
	}

	@Override
	public boolean onPause() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onStop() {
		
		taskManager.stopThread();
		taskManager.endTasks();
		managers.getThreadManager().getTaskProvider().removeTaskManager(taskManager);
		LOG.info("stopped");
		
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
	public void reset() {
		taskManager.reset();
	}

	
	@Override
	public SystemContext getContext() {
		if (taskManager == null)
			return null;
		return taskManager.context;
	}

}
