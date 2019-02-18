package de.felixperko.fractals;

import java.lang.reflect.GenericArrayType;
import java.util.HashMap;
import java.util.UUID;

import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.manager.SystemManager;
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystemFactory;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;

public class FractalsMain {
	
//	public static final String PARAM_SYSTEM = "system";
//	
//	static HashMap<String,String> parameters = new HashMap<>();
	
	static ServerManagers managers;
	
	public static int THREAD_COUNT = 2;
	
	public static void main(String[] args) {
		
		managers = new ServerManagers();

		managers.getSystemManager().insertAvailableSystems();
		
		managers.getThreadManager().startWorkerThreads(THREAD_COUNT);
		managers.getServerNetworkManager().startServerConnectThread();
		//managers.getSystemManager().initSystem(managers.getSystemManager().defaultSystem);
	}
	
//	public static String getParameter(String key, String defaultValue) {
//		if (parameters.containsKey(key))
//			return parameters.get(key);
//		return defaultValue;
//	}

//	private static HashMap<String,String> parseParameters(String[] args) {
//		HashMap<String,String> params = new HashMap<>();
//		for (String arg : args) {
//			String[] s = arg.split(":", 2);
//			if (s.length == 2) {
//				params.put(s[0], s[1]);
//				System.out.println("[main] read parameter "+s[0]+" = "+s[1]);
//			} else {
//				System.err.println("[main] invalid parameter: '"+arg+"'");
//				return null;
//			}
//		}
//		return params;
//	}
}
