package de.felixperko.fractals.system.systems.BreadthFirstSystem;


import static de.felixperko.fractals.system.systems.common.CommonFractalParameters.booleanType;
import static de.felixperko.fractals.system.systems.common.CommonFractalParameters.doubleType;
import static de.felixperko.fractals.system.systems.common.CommonFractalParameters.integerType;
import static de.felixperko.fractals.system.systems.common.CommonFractalParameters.listType;
import static de.felixperko.fractals.system.systems.common.CommonFractalParameters.numberType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.ParamValueField;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.PadovanLayerConfiguration;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.ParamDefinition;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.task.ClassTaskFactory;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskFactory;
import de.felixperko.fractals.system.numbers.Number;

public class BreadthFirstSystem extends AbstractCalcSystem {
	
	private static final Logger LOG = LoggerFactory.getLogger(BreadthFirstSystem.class);
	
	TaskFactory factory_task = new ClassTaskFactory(BreadthFirstTask.class);
	
	BreadthFirstTaskManager taskManager;

	public BreadthFirstSystem(UUID systemId, ServerManagers managers) {
		super(systemId, managers);
	}

	@Override
	public ParamConfiguration createParameterConfiguration() {
		ParamConfiguration config = CommonFractalParameters.getCommonParameterConfiguration();
		
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
		
		List<ParamDefinition> defs_bf = new ArrayList<>();
		List<ParamSupplier> defaultValues = new ArrayList<>();
		defs_bf.add(new ParamDefinition("zoom", "Mapping", StaticParamSupplier.class, numberType)
				.withDescription("The current default coordinate zoom factor."));
		defs_bf.add(new ParamDefinition("width", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The calculation width."));
		defs_bf.add(new ParamDefinition("height", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The calculation height."));
		
		defs_bf.add(new ParamDefinition("limit", "Advanced", StaticParamSupplier.class, numberType)
				.withDescription("Bailout radius. Increase to reduce coloring artifacts, Decrease to improve performance."));
		defs_bf.add(new ParamDefinition("border_generation", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("The chunk distance from rendered area for which chunk calculation should continue."));
		defs_bf.add(new ParamDefinition("border_dispose", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("The chunk distance at which chunks are deleted to preserve memory."));
		defs_bf.add(new ParamDefinition("task_buffer", "Advanced", StaticParamSupplier.class, integerType)
				.withDescription("The amount of tasks that should be buffered for the calculation workers."));
		defs_bf.add(new ParamDefinition("layerConfiguration", "Advanced", StaticParamSupplier.class, layerconfigurationType)
				.withDescription("Manages the layer order in which the calculation is performed."));
			
		NumberFactory nf = new NumberFactory(Number.class, ComplexNumber.class);
		defaultValues.add(new StaticParamSupplier("limit", nf.createNumber(256.0)));
		defaultValues.add(new StaticParamSupplier("border_generation", 0.0));
		defaultValues.add(new StaticParamSupplier("border_dispose", 7.0));
		defaultValues.add(new StaticParamSupplier("task_buffer", 5));
        List<Layer> layers = new ArrayList<>();
        layers.add(new BreadthFirstLayer(CommonFractalParameters.DEFAULT_CHUNK_SIZE).with_samples(1).with_rendering(true).with_priority_shift(0));
//        layers.add(new BreadthFirstUpsampleLayer(16, BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(1).with_rendering(true).with_priority_shift(0));
//        layers.add(new BreadthFirstUpsampleLayer(8, BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(1).with_rendering(true).with_priority_shift(10));
//        layers.add(new BreadthFirstUpsampleLayer(4, BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(1).with_rendering(true).with_priority_shift(20));
//        layers.add(new BreadthFirstUpsampleLayer(2, BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(1).with_rendering(true).with_priority_shift(30));
//        layers.add(new BreadthFirstLayer(BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(1).with_rendering(true).with_priority_shift(40));
//        layers.add(new BreadthFirstLayer(BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(4).with_rendering(true).with_priority_shift(50));
//        layers.add(new BreadthFirstLayer(BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(16).with_rendering(true).with_priority_shift(60));
//        layers.add(new BreadthFirstLayer(BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(49).with_rendering(true).with_priority_shift(70));
//        layers.add(new BreadthFirstLayer(BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(100).with_rendering(true).with_priority_shift(80));
//        layers.add(new BreadthFirstLayer(BFOrbitCommon.DEFAULT_CHUNK_SIZE).with_samples(400).with_rendering(true).with_priority_shift(90));
		defaultValues.add(new StaticParamSupplier("layerConfiguration", new PadovanLayerConfiguration(layers)));
		
		config.addParameterDefinitions(defs_bf);
		config.addDefaultValues(defaultValues);

		for (ParamDefinition def : config.getParameters())
			def.setResetRendererOnChange(false);
		
		config.addListTypes("layers", layerType, upsampleLayerType);
		return config;
	}

	@Override
	public boolean onInit(ParamContainer paramContainer) {
		
		LOG.info("initializing");
		taskManager = new BreadthFirstTaskManager(managers, this);
		taskManager.getSystemContext().setParamConfiguration(getParameterConfiguration());
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
		managers.getSystemManager().removeSystem(this);
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
