package de.felixperko.fractals.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystemFactory;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;

public class SystemManager {
	
	HashMap<UUID, CalcSystem> activeSystems;
	
	HashMap<String, ClassSystemFactory> availableSystems = new HashMap<>();
	
	String defaultSystem = "BasicSystem";
//	CalcSystemFactory systemFactory;
	
	Managers managers;
	
	public SystemManager(Managers managers) {
		this.managers = managers;
	}
	
	public void insertAvailableSystems() {
		availableSystems.put("BasicSystem", new ClassSystemFactory(BasicSystem.class));
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
			if (!oldDataMap.containsKey(e.getKey())) {
//				newSystemClientData.put(e.getKey(), e.getValue());
				CalcSystem system = activeSystems.get(e.getKey());
				if (system == null)
					continue;
				system.addClient(newConfiguration, e.getValue().getClientParameters());
			}else {
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
		requestLoop :
		for (SystemClientData data : newConfiguration.getSystemRequests()) {
			//search systems if applicable
			for (CalcSystem system : activeSystems.values()) {
				if (system.isApplicable(newConfiguration.getConnection(), data.getClientParameters())) {
					system.addClient(newConfiguration, data.getClientParameters());
					continue requestLoop;
				}
			}
			//no existing system applicable -> create new
			CalcSystem system = initSystem(data);
			if (system != null) {
				system.addClient(newConfiguration, data.getClientParameters());
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
		return system;
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
