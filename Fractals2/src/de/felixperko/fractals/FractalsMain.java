package de.felixperko.fractals;

import java.lang.reflect.GenericArrayType;
import java.util.HashMap;
import java.util.UUID;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.manager.server.SystemManager;
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystemFactory;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;

public class FractalsMain {
	
	static ServerManagers managers;
	
	public static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	public static int THREAD_COUNT = 2;
	
	public static void main(String[] args) {
		
		managers = new ServerManagers();

		managers.getSystemManager().insertAvailableSystems();
		
		managers.getThreadManager().startWorkerThreads(THREAD_COUNT);
		managers.getServerNetworkManager().startServerConnectThread();
		
	}
}
