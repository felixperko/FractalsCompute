package de.felixperko.fractals;

import java.lang.reflect.GenericArrayType;
import java.util.HashMap;
import java.util.UUID;

import de.felixperko.fractals.network.NetworkManager;
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.CalcSystemFactory;
import de.felixperko.fractals.system.systems.infra.ClassSystemFactory;

public class FractalsMain {
	
	public static final String PARAM_SYSTEM = "system";
	
	static HashMap<String,String> parameters = new HashMap<>();
	
	static String defaultSystem = "BasicSystem";
	static CalcSystemFactory systemFactory;
	
	static HashMap<UUID, CalcSystem> activeSystems;
	
	static HashMap<String, Class<? extends CalcSystem>> availableSystems = new HashMap<>();
	
	static ThreadManager threadManager;
	static NetworkManager networkManager;
	
	public static void main(String[] args) {
		if ((parameters = parseParameters(args)) == null)
			System.exit(0);
		
		insertAvailableSystems();
		
		threadManager = new ThreadManager();
		networkManager = new NetworkManager(threadManager);
		
		initSystem();
	}
	
	public static void initSystem() {
		//TODO anpassen
		String systemName = getParameter(PARAM_SYSTEM, defaultSystem);
		if (!availableSystems.containsKey(systemName)) {
			System.err.println("[main] system not available: '"+systemName+"'");
			System.exit(0);
		}
		systemFactory = new ClassSystemFactory(availableSystems.get(systemName));
		CalcSystem system = systemFactory.createSystem();
		system.init(null);
		system.start();
	}
	
	private static void insertAvailableSystems() {
		availableSystems.put("BasicSystem", BasicSystem.class);
	}
	
	public static String getParameter(String key, String defaultValue) {
		if (parameters.containsKey(key))
			return parameters.get(key);
		return defaultValue;
	}

	private static HashMap<String,String> parseParameters(String[] args) {
		HashMap<String,String> params = new HashMap<>();
		for (String arg : args) {
			String[] s = arg.split(":", 2);
			if (s.length == 2) {
				params.put(s[0], s[1]);
				System.out.println("[main] read parameter "+s[0]+" = "+s[1]);
			} else {
				System.err.println("[main] invalid parameter: '"+arg+"'");
				return null;
			}
		}
		return params;
	}
}
