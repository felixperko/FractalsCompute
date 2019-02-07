package de.felixperko.fractals.system.systems.BasicSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ThreadManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ClientConnection;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.parameters.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.CoordinateParamSupplier;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.parameters.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.task.ClassTaskFactory;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.TaskFactory;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public class BasicSystem extends AbstractCalcSystem {

	public BasicSystem(Managers managers) {
		super(managers);
	}

	public static final int THREAD_COUNT = 12;

	TaskFactory factory_task = new ClassTaskFactory(BasicTask.class);
	
	BasicTaskManager taskManager;
	CalculateFractalsThread calcThread;
	CalculateFractalsThread calcThread2;
	
	List<FractalsThread> managedThreads = new ArrayList<>();
	
	List<ClientConfiguration> clients = new ArrayList<>();
	
	List<ParamSupplier> systemRelevantParameters = new ArrayList<>();
	
	@Override
	public boolean onInit(HashMap<String, String> settings) {
		NumberFactory numberFactory = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		Map<String, ParamSupplier> params = new HashMap<>();
		int samplesDim = 2;
		params.put("width", new StaticParamSupplier("width", (Integer)4000));
		params.put("height", new StaticParamSupplier("height", (Integer)4000));
		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(0.251), new DoubleNumber(0.00004849892910689283399687005))));
		params.put("zoom", new StaticParamSupplier("zoom", numberFactory.createNumber(4./50000.)));
		params.put("iterations", new StaticParamSupplier("iterations", (Integer)50000));
		params.put("samples", new StaticParamSupplier("samples", (Integer)(samplesDim*samplesDim)));
		
		params.put("start", new StaticParamSupplier("start", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
//		params.put("c", new StaticParamSupplier("c", new DoubleComplexNumber(new DoubleNumber(0.5), new DoubleNumber(0.3))));
//		params.put("start", new CoordinateParamSupplier("start", numberFactory));
		params.put("c", new CoordinateBasicShiftParamSupplier("c", numberFactory, samplesDim));
		params.put("pow", new StaticParamSupplier("pow", new DoubleComplexNumber(new DoubleNumber(2), new DoubleNumber(0))));
		params.put("limit", new StaticParamSupplier("limit", (Double)100.));
		
//		params.put("c", new StaticParamSupplier("c", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
//		params.put("start", new CoordinateBasicShiftParamSupplier("start", numberFactory, samplesDim));
//		//params.put("pow", new StaticParamSupplier("pow", new DoubleComplexNumber(new DoubleNumber(2), new DoubleNumber(Math.PI))));
//		params.put("limit", new StaticParamSupplier("limit", (Double)(0.2)));
		
		managedThreads.add(taskManager = new BasicTaskManager(managers, this));
		taskManager.setParameters(params);
		
		LocalTaskProvider taskProvider = new LocalTaskProvider(taskManager);
		
		for (int i = 0 ; i < THREAD_COUNT ; i++){
			managedThreads.add(new CalculateFractalsThread(managers, this, taskProvider));
		}
//		managedThreads.add(calcThread = new CalculateFractalsThread(this, taskProvider));
//		managedThreads.add(calcThread2 = new CalculateFractalsThread(this, taskProvider));
		//calcThread2 = new CalculateFractalsThread(this, taskProvider);
		return true;
	}

	@Override
	public boolean onStart() {
		
		for (FractalsThread thread : managedThreads)
			thread.start();
		
		taskManager.startTasks();
		return true;
	}

	@Override
	public boolean onPause() {
		return true;
	}

	@Override
	public boolean onStop() {

		for (FractalsThread thread : managedThreads)
			thread.stopThread();
		
		return true;
	}

	
	@Override
	public void reset() {
		taskManager.reset();
	}

	
	@Override
	public void addClient(ClientConfiguration newConfiguration, Map<String, ParamSupplier> parameters) {
		clients.add(newConfiguration);
		taskManager.setParameters(parameters);
	}

	
	@Override
	public void changedClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration) {
		
		Map<String, ParamSupplier> newParameters = newConfiguration.getSystemClientData(getId()).getClientParameters();
		
		boolean applicable = isApplicable(newConfiguration.getConnection(), newParameters);
		
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
	public void removeClient(ClientConfiguration oldConfiguration) {
		clients.remove(oldConfiguration);
	}

	@Override
	public void changeClientMaxThreadCount(int newGranted, int oldGranted) {
		//TODO client threads
	}
	
	public List<ClientConfiguration> getClients(){
		return clients;
	}

}
