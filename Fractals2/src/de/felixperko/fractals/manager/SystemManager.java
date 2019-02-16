package de.felixperko.fractals.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class SystemManager extends Manager{
	
	Map<UUID, CalcSystem> activeSystems = new HashMap<>();
	
	Map<String, ClassSystemFactory> availableSystems = new HashMap<>();
	
	public String defaultSystem = "BasicSystem";
//	CalcSystemFactory systemFactory;
	
	public SystemManager(ServerManagers managers) {
		super(managers);
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
				if (system.isApplicable(newConfiguration.getConnection(), data.getClientParameters())) {
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
