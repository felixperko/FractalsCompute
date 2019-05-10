package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.SystemConnectedMessage;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.ParamValueField;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.parameters.ParameterDefinition;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.task.ClassTaskFactory;
import de.felixperko.fractals.system.task.TaskFactory;
import de.felixperko.fractals.util.CategoryLogger;

public class BreadthFirstSystem extends AbstractCalcSystem {
	
	CategoryLogger log;
	
	TaskFactory factory_task = new ClassTaskFactory(BreadthFirstTask.class);
	
	BreadthFirstTaskManagerNew taskManager;
	
	List<ClientConfiguration> clients = new ArrayList<>();
	
	ParameterConfiguration parameterConfiguration;

	public BreadthFirstSystem(ServerManagers managers) {
		super(managers);
		log = managers.getSystemManager().getLogger().createSubLogger(getNumber()+"_BF");
		createParameterConfiguration();
	}

	private void createParameterConfiguration() {

		parameterConfiguration = new ParameterConfiguration();
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
		ParamValueType layerType = new ParamValueType("Layer",
				new ParamValueField("priority_shift", doubleType, 0d),
				new ParamValueField("priority_multiplier", doubleType, 0d),
				new ParamValueField("samples", integerType, 1));
		ParamValueType upsampleLayerType = new ParamValueType("UpsampleLayer", 
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
		/**
		 * integer:
		 * width, height, chunkSize, iterations, samples,
		 * task_buffer
		 * 
		 * double:
		 * border_generation, border_dispose, limit
		 * 
		 * number:
		 * zoom
		 * 
		 * complexnumber:
		 * midpoint, start, c, pow
		 * 
		 * string:
		 * calculator, systemName
		 * 
		 * numberfactory: numberFactory
		 * arraychunkfactory: chunkFactory
		 * 
		 * 
		 * width -> integer
		 * midpoint -> complexnumber
		 * zoom -> number
		 * layers -> List<BreadthFirstLayer>
		 * calculator -> String from List
		 * 
		 */
		List<Class<? extends ParamSupplier>> varList = new ArrayList<>();
		varList.add(StaticParamSupplier.class);
		varList.add(CoordinateBasicShiftParamSupplier.class);

		List<ParameterDefinition> defs = new ArrayList<>();
		defs.add(new ParameterDefinition("iterations", StaticParamSupplier.class, integerType));
		defs.add(new ParameterDefinition("calculator", StaticParamSupplier.class, selectionType));
		defs.add(new ParameterDefinition("pow", varList, complexnumberType));
		defs.add(new ParameterDefinition("c", varList, complexnumberType));
		defs.add(new ParameterDefinition("start", varList, complexnumberType));
		defs.add(new ParameterDefinition("layers", StaticParamSupplier.class, layerconfigurationType));
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
		
		parameterConfiguration.addParameterDefinitions(defs);
		
//		Selection<Class<? extends FractalsCalculator>> calculatorSelection = new Selection<>("calculator");
//		calculatorSelection.addOption("Mandelbrot", MandelbrotCalculator.class);
//		calculatorSelection.addOption("BurningShip", BurningShipCalculator.class);
//		parameterConfiguration.addSelection(calculatorSelection);
		Selection<String> calculatorSelection = new Selection<>("calculator");
		calculatorSelection.addOption("Mandelbrot", "MandelbrotCalculator");
		calculatorSelection.addOption("BurningShip", "BurningShipCalculator");
		parameterConfiguration.addSelection(calculatorSelection);
		
		Selection<String> systemNameSelection = new Selection<>("systemName");
		systemNameSelection.addOption("BreadthFirstSystem", "BreadthFirstSystem");
		parameterConfiguration.addSelection(systemNameSelection);
	}

	@Override
	public void reset() {
		taskManager.reset();
	}

	@Override
	public void addClient(ClientConfiguration newConfiguration, SystemClientData systemClientData) {
		synchronized (clients) {
			clients.add(newConfiguration);
			newConfiguration.getSystemRequests().remove(systemClientData);
			newConfiguration.getSystemClientData().put(id, systemClientData);
			newConfiguration.getConnection().writeMessage(new SystemConnectedMessage(id, newConfiguration, getParameterConfiguration()));
			taskManager.setParameters(systemClientData.getClientParameters());
		}
	}

	@Override
	public void changedClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration) {
		
		Map<String, ParamSupplier> newParameters = newConfiguration.getSystemClientData(getId()).getClientParameters();
		
		boolean applicable = isApplicable(newConfiguration.getConnection(), newParameters);
		System.out.println("is Applicable()? "+applicable);
		synchronized(clients) {
			if (oldConfiguration != null)
				clients.remove(oldConfiguration);
			if (applicable) {
				clients.add(newConfiguration);
				taskManager.setParameters(newParameters);
			}
		}
	}

	@Override
	public void removeClient(ClientConfiguration oldConfiguration) {
		synchronized(clients) {
			clients.remove(oldConfiguration);
			if (clients.isEmpty())
				stop();
		}
	}

	@Override
	public void changeClientMaxThreadCount(int newGranted, int oldGranted) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isApplicable(ClientConnection connection, Map<String, ParamSupplier> parameters) {
		boolean hasClient = false;
		for (ClientConfiguration conf : clients) {
			if (conf.getConnection() == connection) {
				hasClient = true;
				break;
			}
		}
		if (hasClient && clients.size() == 1)
			return true;
		for (ParamSupplier param : parameters.values()) {
			if (param.isSystemRelevant() || param.isLayerRelevant() || param.isViewRelevant()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onInit(Map<String, ParamSupplier> params) {
		
		log.log("initializing");
		taskManager = new BreadthFirstTaskManagerNew(managers, this);
		taskManager.setParameters(params);
		
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

	public List<ClientConfiguration> getClients() {
		return clients;
	}
	
	public CategoryLogger getLogger(){
		return log;
	}

	public ParameterConfiguration getParameterConfiguration() {
		return parameterConfiguration;
	}

}
