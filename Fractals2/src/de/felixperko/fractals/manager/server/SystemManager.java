package de.felixperko.fractals.manager.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

public class SystemManager extends Manager{
	
	HashMap<UUID, WeakHashMap<Integer, FractalsTask>> tasks = new HashMap<>();
	
	CategoryLogger log = new CategoryLogger("systems", ColorContainer.YELLOW);
	
	Map<UUID, CalcSystem> activeSystems = new HashMap<>();
	
	ServerStateInfo stateInfo = new ServerStateInfo();
	
	Map<String, ClassSystemFactory> availableSystems = new HashMap<>();
	
	public String defaultSystem = "BasicSystem";
//	CalcSystemFactory systemFactory;
	
	public SystemManager(ServerManagers managers) {
		super(managers);
	}
	
	public void insertAvailableSystems() {
		//availableSystems.put("BasicSystem", new ClassSystemFactory(BasicSystem.class));
		availableSystems.put("BreadthFirstSystem", new ClassSystemFactory(BreadthFirstSystem.class));
	}

//	Map<UUID, SystemInstanceClientData> newSystemClientData = new HashMap<>();
	
	public void changedClientConfiguration(ClientConfiguration oldConfiguration, ClientConfiguration newConfiguration) {
		
		Map<UUID, SystemClientData> oldDataMap;
		if (oldConfiguration != null)
			oldDataMap = oldConfiguration.getSystemClientData();
		else
			oldDataMap = new HashMap<>();
		
		Map<UUID, SystemClientData> newDataMap = newConfiguration.getSystemClientData();
		
		//evaluate parameter changes for existing systems
		for (Entry<UUID, SystemClientData> e : newDataMap.entrySet()) {
			if (!oldDataMap.containsKey(e.getKey())) {//new parameter (didn't exist in oldDataMap)
//				newSystemClientData.put(e.getKey(), e.getValue());
				CalcSystem system = activeSystems.get(e.getKey());
				if (system == null)
					continue;
				system.addClient(newConfiguration, e.getValue());
			} else {//parameter was changed
				boolean changed = false;
				SystemClientData newData = e.getValue();
				SystemClientData oldData = null;
				if (oldConfiguration != null) {
					oldData = oldConfiguration.getSystemClientData(e.getKey());
					for (Entry<String, ParamSupplier> param : newData.getClientParameters().entrySet()) {
						String paramName = param.getKey();
						ParamSupplier newParam = param.getValue();
						ParamSupplier oldParam = oldData.getClientParameter(paramName);
						if (!newParam.equals(oldParam)) {
							changed = true;
						}
					}
				}
				
				int oldGranted = oldData != null ? oldData.getGrantedThreads() : 0;
				int newGranted = newData.getGrantedThreads();
				
				if (!changed && oldGranted == newGranted) {
					continue;
				}
				
				CalcSystem system = activeSystems.get(e.getKey());
				if (system == null)
					continue;

				if (changed)
					system.changedClient(newConfiguration, oldConfiguration);
				
				if (oldGranted != newGranted) {
					system.changeClientMaxThreadCount(newGranted, oldGranted);
				}
			}
		}
		
		//unregister from systems
		for (UUID systemId : oldDataMap.keySet()) {
			if (!newDataMap.containsKey(systemId)) {
				CalcSystem system = activeSystems.get(systemId);
				if (system == null)
					continue;
				system.removeClient(oldConfiguration);
			}
		}
		
		//process system requests
		List<SystemClientData> requests = new ArrayList<>(newConfiguration.getSystemRequests());
		requestLoop :
		for (SystemClientData data : requests) {
			//search systems if applicable
			for (CalcSystem system : activeSystems.values()) {
				if ((system.getLifeCycleState() == LifeCycleState.IDLE || system.getLifeCycleState() == LifeCycleState.RUNNING)
					&& system.isApplicable(newConfiguration.getConnection(), data.getClientParameters())) {
					system.addClient(newConfiguration, data);
					continue requestLoop;
				}
			}
			//no existing system applicable -> create new
			CalcSystem system = initSystem(data);
			if (system != null) {
				system.init(data.getClientParameters());
				system.addClient(newConfiguration, data);
				system.start();
			}
		}
	}

	private CalcSystem initSystem(SystemClientData data) {
		ParamSupplier systemNameParam = data.getClientParameter("systemName");
		if (systemNameParam == null) {
			System.err.println("Invalid system request: systemName parameters not set.");
			return null;
		}
		String systemName = (String) systemNameParam.get(0, 0);
		if (!availableSystems.containsKey(systemName)) {
			System.err.println("Invalid system request: system for name doesn't exist: "+systemName);
			return null;
		}
		
		CalcSystem system = availableSystems.get(systemName).createSystem(managers);
		activeSystems.put(system.getId(), system);
		log.log("initiating system "+systemName);
		return system;
	}

	public ServerStateInfo getStateInfo() {
		return stateInfo;
	}
	
	public CategoryLogger getLogger(){
		return log;
	}


	public void clientRemoved(ClientConfiguration conf) {
		for (UUID systemId : conf.getSystemClientData().keySet()) {
			CalcSystem system = activeSystems.get(systemId);
			system.removeClient(conf);
			//TODO stop connection (i/o!)		
		}
	}
	
	public void addTask(FractalsTask task){
		getTaskMap(task.getStateInfo().getSystemId()).put(task.getId(), task);
	}
	
	public void removeTask(FractalsTask task){
		getTaskMap(task.getStateInfo().getSystemId()).remove(task.getId());
	}
	
	public FractalsTask getTask(UUID systemId, Integer taskId) {
		return getTaskMap(systemId).get(taskId);
	}
	
	private WeakHashMap<Integer, FractalsTask> getTaskMap(UUID systemId) {
		WeakHashMap<Integer, FractalsTask> map = tasks.get(systemId);
		if (map == null) {
			map = new WeakHashMap<Integer, FractalsTask>();
			tasks.put(systemId, map);
		}
		return map;
	}

	public boolean updateTaskState(TaskStateUpdate update) {
		FractalsTask task = getTask(update.getSystemId(), update.getTaskId());
		if (task != null){
			TaskStateInfo tsi = task.getStateInfo();
			if (update.getLayerId() != tsi.getLayerId())
				return false;
			tsi.setProgress(update.getProgress());
			tsi.setState(update.getTaskState());
			return true;
		}
		return false;
	}

//	public CalcSystem initSystem(String systemName) {
//		if (!availableSystems.containsKey(systemName)) {
//			System.err.println("[main] system not available: '"+systemName+"'");
//			System.exit(0);
//		}
//		systemFactory = new ClassSystemFactory(availableSystems.get(systemName));
//		CalcSystem system = systemFactory.createSystem(managers.getThreadManager());
//		system.init(null);
//		system.start();
//		return system;
//	}
	
	

}
