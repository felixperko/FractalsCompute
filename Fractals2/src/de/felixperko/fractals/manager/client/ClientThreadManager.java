package de.felixperko.fractals.manager.client;

import de.felixperko.fractals.manager.common.ThreadManager;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.RemoteTaskProvider;
import de.felixperko.fractals.system.task.TaskProvider;

public class ClientThreadManager extends ThreadManager {
	
	RemoteTaskProvider taskProvider;

	public ClientThreadManager(ClientManagers managers) {
		super(managers);
		taskProvider = new RemoteTaskProvider(managers, 5);
	}

}