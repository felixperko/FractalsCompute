package de.felixperko.fractals.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SystemInstanceClientData;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystemFactory;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;

public class SystemManager {
	
	static HashMap<UUID, CalcSystem> activeSystems;
	
	static HashMap<String, Class<? extends CalcSystem>> availableSystems = new HashMap<>();
	
	public static String defaultSystem = "BasicSystem";
	static CalcSystemFactory systemFactory;
	
	Managers managers;
	
	public SystemManager(Managers managers) {
		this.managers = managers;
	}
	
	public void insertAvailableSystems() {
		availableSystems.put("BasicSystem", BasicSystem.class);
	}

//	Map<UUID, SystemInstanceClientData> newSystemClientData = new HashMap<>();
	
	public void changedClientConfiguration(ClientConfiguration oldConfiguration, ClientConfiguration newConfiguration) {
		Map<UUID, SystemInstanceClientData> oldDataMap = oldConfiguration.getSystemClientData();
		Map<UUID, SystemInstanceClientData> newDataMap = newConfiguration.getSystemClientData();
		for (Entry<UUID, SystemInstanceClientData> e : newConfiguration.getSystemClientData().entrySet()) {
			if (!oldDataMap.containsKey(e.getKey())) {
//				newSystemClientData.put(e.getKey(), e.getValue());
				CalcSystem system = activeSystems.get(e.getKey());
				if (system == null)
					continue;
				system.addClient(newConfiguration);
			}else {
				boolean changed = false;
				SystemInstanceClientData newData = e.getValue();
				SystemInstanceClientData oldData = oldConfiguration.getSystemClientData(e.getKey());
				for (Entry<String, ParamSupplier> param : newData.getClientParameters().entrySet()) {
					String paramName = param.getKey();
					ParamSupplier newParam = param.getValue();
					ParamSupplier oldParam = oldData.getClientParameter(paramName);
					if (!newParam.equals(oldParam)) {
						changed = true;
					}
				}
				
				int oldGranted = oldData.getGrantedThreads();
				int newGranted = oldData.getGrantedThreads();
				
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
		for (UUID systemId : oldDataMap.keySet()) {
			if (!newDataMap.containsKey(systemId)) {
				CalcSystem system = activeSystems.get(systemId);
				if (system == null)
					continue;
				system.removeClient(oldConfiguration);
			}
		}
	}

	public void initSystem(String systemName) {
		if (!availableSystems.containsKey(systemName)) {
			System.err.println("[main] system not available: '"+systemName+"'");
			System.exit(0);
		}
		systemFactory = new ClassSystemFactory(availableSystems.get(systemName));
		CalcSystem system = systemFactory.createSystem(managers.getThreadManager());
		system.init(null);
		system.start();
	}

}
