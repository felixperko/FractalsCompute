package de.felixperko.fractals.manager.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstSystem;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainerListener;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;
import de.felixperko.fractals.system.task.FractalsTask;

public class SystemManager extends Manager{
	
	HashMap<UUID, WeakHashMap<Integer, FractalsTask>> tasks = new HashMap<>();
	
	private static final Logger LOG = LoggerFactory.getLogger(SystemManager.class);
	
	Map<UUID, CalcSystem> activeSystems = new HashMap<>();
	
	ServerStateInfo stateInfo = new ServerStateInfo();
	
	Map<String, ClassSystemFactory> availableSystems = new HashMap<>();
	
	public String defaultSystem = "BreadthFirstSystem";
//	CalcSystemFactory systemFactory;
	
	public SystemManager(ServerManagers managers) {
		super(managers);
	}
	
	public void registerAvailableSystems() {
		//availableSystems.put("BasicSystem", new ClassSystemFactory(BasicSystem.class));
		availableSystems.put("BreadthFirstSystem", new ClassSystemFactory(BreadthFirstSystem.class));
	}

//	Map<UUID, SystemInstanceClientData> newSystemClientData = new HashMap<>();
	
	public void changedClientConfiguration(ClientConfiguration oldConfiguration, ClientConfiguration newConfiguration) {
		
		Map<UUID, ParamContainer> oldDataMap;
		if (oldConfiguration != null)
			oldDataMap = oldConfiguration.getParamContainers();
		else
			oldDataMap = new HashMap<>();
		
		Map<UUID, ParamContainer> newDataMap = newConfiguration.getParamContainers();
		
		//evaluate parameter changes for existing systems
		for (Entry<UUID, ParamContainer> e : newDataMap.entrySet()) {
			
			UUID systemId = e.getKey();
			ParamContainer paramContainer = e.getValue();
			
			if (!oldDataMap.containsKey(systemId)) {//new parameter (didn't exist in oldDataMap)
				CalcSystem system = activeSystems.get(systemId);
				if (system == null)
					continue;
				system.addClient(newConfiguration, paramContainer);
			} else {//parameter was changed
				boolean changed = false;
				ParamContainer oldData = null;
				if (oldConfiguration != null) {
					oldData = oldConfiguration.getParamContainer(e.getKey());
					for (Entry<String, ParamSupplier> param : paramContainer.getParamMap().entrySet()) {
						String paramName = param.getKey();
						ParamSupplier newParam = param.getValue();
						ParamSupplier oldParam = oldData.getParam(paramName);
						if (!newParam.equals(oldParam)) {
							changed = true;
						}
					}
				}
				
				if (!changed) {
					continue;
				}
				
				CalcSystem system = activeSystems.get(e.getKey());
				if (system == null)
					continue;

				if (changed)
					system.changeClient(newConfiguration, oldConfiguration);
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
		Map<UUID, ParamContainer> requests = new HashMap<>(newConfiguration.getSystemRequests());
		requestLoop :
		for (Entry<UUID, ParamContainer> e : requests.entrySet()) {
			UUID systemId = e.getKey();
			ParamContainer data = e.getValue();
			
			//skip existing
			if (activeSystems.containsKey(systemId))
				continue;
			
//			//search systems if applicable
//			for (CalcSystem system : activeSystems.values()) {
//				if ((system.getLifeCycleState() == LifeCycleState.IDLE || system.getLifeCycleState() == LifeCycleState.RUNNING)
//					&& system.isApplicable(newConfiguration.getConnection(), data)) {
//					system.addClient(newConfiguration, data);
//					continue requestLoop;
//				}
//			}
			//no existing system applicable -> create new
			CalcSystem system = initSystem(systemId, data);
			if (system != null) {
				system.init(data);
				system.addClient(newConfiguration, data);
				system.start();
			}
		}
	}

	private CalcSystem initSystem(UUID systemId, ParamContainer data) {
		ParamSupplier systemNameParam = data.getParam(CommonFractalParameters.PARAM_SYSTEMNAME);
//		if (systemNameParam == null) {
//			LOG.error("Invalid system request: systemName parameters not set.");
//			return null;
//		}
		String systemName = systemNameParam == null ? defaultSystem : (String) systemNameParam.get(null, null, 0, 0);
		if (!availableSystems.containsKey(systemName)) {
			//TODO reply to client
			LOG.error("Invalid system request: system for name doesn't exist: "+systemName);
			return null;
		}
		
		ClassSystemFactory systemFactory = availableSystems.get(systemName);
		CalcSystem system = systemFactory.createSystem(systemId, managers); 
		activeSystems.put(system.getId(), system);
		LOG.info("initiating system "+systemName);
		return system;
	}
	
	public void removeSystem(CalcSystem system){
		activeSystems.remove(system);
	}

	public ServerStateInfo getStateInfo() {
		return stateInfo;
	}

	public void clientRemoved(ClientConfiguration conf) {
		for (UUID systemId : conf.getParamContainers().keySet()) {
			CalcSystem system = activeSystems.get(systemId);
			if (system != null)
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

	public boolean registerViewContainerListener(UUID systemId, ViewContainerListener viewContainerListener) {
		SystemContext context = getContextForId(systemId);
		if (context == null)
			return false;
		return context.getViewContainer().registerViewContainerListener(viewContainerListener);
	}
	
	public boolean unregisterViewContainerListener(UUID systemId,  ViewContainerListener viewContainerListener) {
		SystemContext context = getContextForId(systemId);
		if (context == null)
			return false;
		return context.getViewContainer().unregisterViewContainerListener(viewContainerListener);
	}
	
	private SystemContext getContextForId(UUID systemId) {
		CalcSystem sys = activeSystems.get(systemId);
		if (sys == null)
			return null;
		return sys.getContext();
	}
	
//	public CalcSystem initSystem(String systemName) {
//		if (!availableSystems.containsKey(systemName)) {
//			LOG.error("[main] system not available: '"+systemName+"'");
//			System.exit(0);
//		}
//		systemFactory = new ClassSystemFactory(availableSystems.get(systemName));
//		CalcSystem system = systemFactory.createSystem(managers.getThreadManager());
//		system.init(null);
//		system.start();
//		return system;
//	}
	
	

}
