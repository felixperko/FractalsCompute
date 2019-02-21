package de.felixperko.fractals.system.systems.BasicSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.SystemConnectedMessage;
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

	public BasicSystem(ServerManagers managers) {
		super(managers);
	}

	TaskFactory factory_task = new ClassTaskFactory(BasicTask.class);
	
	BasicTaskManager taskManager;
	
	List<ClientConfiguration> clients = new ArrayList<>();
	
	@Override
	public boolean onInit(Map<String, ParamSupplier> params) {
		
		taskManager = new BasicTaskManager(managers, this);
		taskManager.setParameters(params);
		
		return true;
	}

	@Override
	public boolean onStart() {
		taskManager.start();
		taskManager.startTasks();
		managers.getThreadManager().getTaskProvider().addTaskManager(taskManager);
		return true;
	}

	@Override
	public boolean onPause() {
		return true;
	}

	@Override
	public boolean onStop() {
		taskManager.stopThread();
		managers.getThreadManager().getTaskProvider().removeTaskManager(taskManager);
		return true;
	}

	
	@Override
	public void reset() {
		taskManager.reset();
	}

	
	@Override
	public void addClient(ClientConfiguration newConfiguration, SystemClientData systemClientData) {
		clients.add(newConfiguration);
		newConfiguration.getSystemRequests().remove(systemClientData);
		newConfiguration.getSystemClientData().put(id, systemClientData);
		newConfiguration.getConnection().writeMessage(new SystemConnectedMessage(id, newConfiguration));
		taskManager.setParameters(systemClientData.getClientParameters());
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
