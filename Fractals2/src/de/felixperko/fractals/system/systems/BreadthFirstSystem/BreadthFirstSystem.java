package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
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
				new ParamValueField("culling", booleanType, true));
		
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
		defs.add(new ParameterDefinition("iterations", "Basic", StaticParamSupplier.class, integerType)
				.withDescription("The maximum number of iterations until a sample is marked as unsuccessful.\n"
						+ "Increase to allow more black regions to be filled at the cost of slower computation."));
		defs.add(new ParameterDefinition("calculator", "Basic", StaticParamSupplier.class, selectionType)
				.withDescription("Choose the calculator to render different fractals."));
		
		defs.add(new ParameterDefinition("zoom", "Position", StaticParamSupplier.class, numberType)
				.withDescription("The current default coordinate zoom factor."));
		defs.add(new ParameterDefinition("midpoint", "Position", StaticParamSupplier.class, complexnumberType)
				.withDescription("The current default coordinate center."));
		defs.add(new ParameterDefinition("numberFactory", "Position", StaticParamSupplier.class, numberfactoryType)
				.withDescription("Manages the used number and complex number class."));
		defs.add(new ParameterDefinition("width", "Position", StaticParamSupplier.class, integerType)
				.withDescription("The calculation width."));
		defs.add(new ParameterDefinition("height", "Position", StaticParamSupplier.class, integerType)
				.withDescription("The calculation height."));
		
		defs.add(new ParameterDefinition("layerConfiguration", "Advanced", StaticParamSupplier.class, layerconfigurationType)
				.withDescription("Manages the layer order in which the calculation is performed."));
		defs.add(new ParameterDefinition("chunkFactory", "Advanced", StaticParamSupplier.class, arraychunkfactoryType)
				.withDescription("Details about the chunks, e.g. the size of used chunks."));
		defs.add(new ParameterDefinition("limit", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("Bailout radius. Increase to reduce coloring artifacts, Decrease to improve performance."));
		defs.add(new ParameterDefinition("border_generation", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("The chunk distance from rendered area for which chunk calculation should continue."));
		defs.add(new ParameterDefinition("border_dispose", "Advanced", StaticParamSupplier.class, doubleType)
				.withDescription("The chunk distance at which chunks are deleted to preserve memory."));
		defs.add(new ParameterDefinition("task_buffer", "Advanced", StaticParamSupplier.class, integerType)
				.withDescription("The amount of tasks that should be buffered for the calculation workers."));
		defs.add(new ParameterDefinition("systemName", "Advanced", StaticParamSupplier.class, selectionType)
				.withDescription("The internal calculation system to use for task management."));
		defs.add(new ParameterDefinition("view", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The current view to calculate for."));

		parameterConfiguration.addParameterDefinitions(defs);

		List<ParameterDefinition> mandelbrot_calculator_defs = new ArrayList<>();
		mandelbrot_calculator_defs.add(new ParameterDefinition("pow", "Calculator", varList, complexnumberType)
				.withDescription("The exponent parameter that is applied at every calculation step.")
				.withHints("ui-element[default]: fields", "ui-element:plane soft-min=-2 soft-max=2"));
		mandelbrot_calculator_defs.add(new ParameterDefinition("c", "Calculator", varList, complexnumberType)
				.withDescription("The shift parameter that is applied at every calculation step.")
				.withHints("ui-element[default]: plane soft-min=-2 soft-max=2", "ui-element:fields"));
		mandelbrot_calculator_defs.add(new ParameterDefinition("start", "Calculator", varList, complexnumberType)
				.withDescription("The input number parameter that is used for the first calculation step.")
				.withHints("ui-element[default]: plane soft-min=-2 soft-max=2", "ui-element:fields"));
		
		parameterConfiguration.addCalculatorParameters("MandelbrotCalculator", mandelbrot_calculator_defs);
		parameterConfiguration.addCalculatorParameters("BurningShipCalculator", mandelbrot_calculator_defs);
		parameterConfiguration.addCalculatorParameters("TricornCalculator", mandelbrot_calculator_defs);
		
		List<ParameterDefinition> newton_calculator_defs = new ArrayList<>();
		newton_calculator_defs.add(new ParameterDefinition("start", "Calculator", CoordinateBasicShiftParamSupplier.class, complexnumberType)
				.withDescription("The start position for Newton's method."));
		
		parameterConfiguration.addCalculatorParameters("NewtonThridPowerMinusOneCalculator", newton_calculator_defs);									//TODO test -> newton_calculator_defs!
		parameterConfiguration.addCalculatorParameters("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", newton_calculator_defs);	//
		
		Selection<String> calculatorSelection = new Selection<>("calculator");
		calculatorSelection.addOption("Mandelbrot", "MandelbrotCalculator", "The famous Mandelbrot set with variations.");
		calculatorSelection.addOption("BurningShip", "BurningShipCalculator", "A modified Mandelbrot set with variations.");
		calculatorSelection.addOption("Tricorn", "TricornCalculator", "A modified Mandelbrot set with variations.");
		calculatorSelection.addOption("Newton x^3 - 1", "NewtonThridPowerMinusOneCalculator", "Newton's method for the Function f(x) = x^3 - 1");
		calculatorSelection.addOption("Newton x^8 + 15x^4 - 16", "NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", "Newton's method for the Function f(x) = x^8 + 15x^4 - 16");
		
		parameterConfiguration.addSelection(calculatorSelection);
		
		Selection<String> systemNameSelection = new Selection<>("systemName");
		systemNameSelection.addOption("BreadthFirstSystem", "BreadthFirstSystem", "The current default real time calculation system.");
		parameterConfiguration.addSelection(systemNameSelection);
		
		Selection<Class<? extends Number<?>>> numberClassSelection = new Selection<>("numberClass");
		numberClassSelection.addOption("double", DoubleNumber.class, "A wrapper for the Java double primitive.");
		parameterConfiguration.addSelection(numberClassSelection);
		
		Selection<Class<? extends ComplexNumber<?, ?>>> complexNumberClassSelection = new Selection<>("complexNumberClass");
		complexNumberClassSelection.addOption("double-complex", DoubleComplexNumber.class, "A complex number wrapping two Java double primitives.");
		parameterConfiguration.addSelection(complexNumberClassSelection);
		
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
