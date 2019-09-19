package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.SystemConnectedMessage;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.parameters.ParamValueField;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.parameters.ParameterDefinition;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.task.ClassTaskFactory;
import de.felixperko.fractals.system.task.TaskFactory;
import de.felixperko.fractals.util.CategoryLogger;

public class BreadthFirstSystem extends AbstractCalcSystem {
	
	CategoryLogger log;
	
	TaskFactory factory_task = new ClassTaskFactory(BreadthFirstTask.class);
	
	BreadthFirstTaskManager taskManager;

	public BreadthFirstSystem(ServerManagers managers) {
		super(managers);
		log = managers.getSystemManager().getLogger().createSubLogger(getNumber()+"_BF");
	}

	@Override
	public ParameterConfiguration createParameterConfiguration() {
		ParameterConfiguration parameterConfiguration = new ParameterConfiguration();
		ParamValueType integerType = new ParamValueType("integer");
		ParamValueType doubleType = new ParamValueType("double");
		ParamValueType booleanType = new ParamValueType("boolean");
		ParamValueType classType = new ParamValueType("class");
		ParamValueType listType = new ParamValueType("list");
		
		ParamValueType numberType = new ParamValueType("number");
		ParamValueType complexnumberType = new ParamValueType("complexnumber");
		
		ParamValueType numberfactoryType = new ParamValueType("numberfactory",
				new ParamValueField("numberClass", classType, DoubleNumber.class),
				new ParamValueField("complexNumberClass", classType, DoubleComplexNumber.class));
		ParamValueType arraychunkfactoryType = new ParamValueType("arraychunkfactory",
				new ParamValueField("chunkClass", classType, ReducedNaiveChunk.class),
				new ParamValueField("chunkSize", integerType, 200));
		ParamValueType layerType = new ParamValueType("BreadthFirstLayer",
				new ParamValueField("priority_shift", doubleType, 0d),
				new ParamValueField("priority_multiplier", doubleType, 0d),
				new ParamValueField("samples", integerType, 1));
		ParamValueType upsampleLayerType = new ParamValueType("BreadthFirstUpsampleLayer", 
				new ParamValueField("priority_shift", doubleType, 0d),
				new ParamValueField("priority_multiplier", doubleType, 0d),
				new ParamValueField("upsample", integerType, 1),
				new ParamValueField("culling", booleanType));
		ParamValueType layerconfigurationType = new ParamValueType("LayerConfiguration",
				new ParamValueField("layers", listType),
				new ParamValueField("simStep", doubleType, 0.05),
				new ParamValueField("simCount", integerType, 20),
				new ParamValueField("seed", integerType, 42));
		ParamValueType selectionType = new ParamValueType("selection");
		ParamValueType[] types = new ParamValueType[] {
				integerType, doubleType, booleanType, classType, listType, numberType, complexnumberType,
				numberfactoryType, arraychunkfactoryType, layerconfigurationType, layerType, upsampleLayerType, selectionType
		};
		parameterConfiguration.addValueTypes(types);
		
		List<Class<? extends ParamSupplier>> varList = new ArrayList<>();
		varList.add(StaticParamSupplier.class);
		varList.add(CoordinateBasicShiftParamSupplier.class);

		List<ParameterDefinition> defs = new ArrayList<>();
		defs.add(new ParameterDefinition("iterations", StaticParamSupplier.class, integerType));
		defs.add(new ParameterDefinition("calculator", StaticParamSupplier.class, selectionType));
		defs.add(new ParameterDefinition("layerConfiguration", StaticParamSupplier.class, layerconfigurationType));
		defs.add(new ParameterDefinition("numberFactory", StaticParamSupplier.class, numberfactoryType));
		defs.add(new ParameterDefinition("chunkFactory", StaticParamSupplier.class, arraychunkfactoryType));
		defs.add(new ParameterDefinition("limit", StaticParamSupplier.class, doubleType));
		defs.add(new ParameterDefinition("border_generation", StaticParamSupplier.class, doubleType));
		defs.add(new ParameterDefinition("border_dispose", StaticParamSupplier.class, doubleType));
		defs.add(new ParameterDefinition("width", StaticParamSupplier.class, integerType));
		defs.add(new ParameterDefinition("height", StaticParamSupplier.class, integerType));
		defs.add(new ParameterDefinition("task_buffer", StaticParamSupplier.class, integerType));
		defs.add(new ParameterDefinition("zoom", StaticParamSupplier.class, numberType));
		defs.add(new ParameterDefinition("midpoint", StaticParamSupplier.class, complexnumberType));
		defs.add(new ParameterDefinition("systemName", StaticParamSupplier.class, selectionType));
		defs.add(new ParameterDefinition("view", StaticParamSupplier.class, integerType));

		parameterConfiguration.addParameterDefinitions(defs);

		List<ParameterDefinition> mandelbrot_calculator_defs = new ArrayList<>();
		mandelbrot_calculator_defs.add(new ParameterDefinition("pow", varList, complexnumberType));
		mandelbrot_calculator_defs.add(new ParameterDefinition("c", varList, complexnumberType));
		mandelbrot_calculator_defs.add(new ParameterDefinition("start", varList, complexnumberType));
		
		parameterConfiguration.addCalculatorParameters("MandelbrotCalculator", mandelbrot_calculator_defs);
		parameterConfiguration.addCalculatorParameters("BurningShipCalculator", mandelbrot_calculator_defs);
		parameterConfiguration.addCalculatorParameters("TricornCalculator", mandelbrot_calculator_defs);
		
		List<ParameterDefinition> newton_calculator_defs = new ArrayList<>();
		newton_calculator_defs.add(new ParameterDefinition("start", CoordinateBasicShiftParamSupplier.class, complexnumberType));
		
		parameterConfiguration.addCalculatorParameters("NewtonThridPowerMinusOneCalculator", newton_calculator_defs);									//TODO test -> newton_calculator_defs!
		parameterConfiguration.addCalculatorParameters("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", newton_calculator_defs);	//
		
		Selection<String> calculatorSelection = new Selection<>("calculator");
		calculatorSelection.addOption("Mandelbrot", "MandelbrotCalculator");
		calculatorSelection.addOption("BurningShip", "BurningShipCalculator");
		calculatorSelection.addOption("Tricorn", "TricornCalculator");
		calculatorSelection.addOption("Newton x^3 - 1", "NewtonThridPowerMinusOneCalculator");
		calculatorSelection.addOption("Newton x^8 + 15x^4 - 16", "NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator");
		parameterConfiguration.addSelection(calculatorSelection);
		
		Selection<String> systemNameSelection = new Selection<>("systemName");
		systemNameSelection.addOption("BreadthFirstSystem", "BreadthFirstSystem");
		parameterConfiguration.addSelection(systemNameSelection);
		
		parameterConfiguration.addListTypes("layers", layerType, upsampleLayerType);
		
		return parameterConfiguration;
	}

	@Override
	public boolean onInit(ParamContainer paramContainer) {
		
		log.log("initializing");
		taskManager = new BreadthFirstTaskManager(managers, this);
		taskManager.setParameters(paramContainer);
		
		return true;
	}

	@Override
	public boolean onStart() {

		log.log("starting");
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
		log.log("stopped");
		
		return true;
	}

	@Override
	public void addedClient(ClientConfiguration newConfiguration, SystemClientData systemClientData) {
		taskManager.setParameters(systemClientData);
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
	public void changeClientMaxThreadCount(int newGranted, int oldGranted) {
		// TODO Auto-generated method stub

	}
	
	public CategoryLogger getLogger(){
		return log;
	}

	
	@Override
	public SystemContext getContext() {
		if (taskManager == null)
			return null;
		return taskManager.context;
	}

}
