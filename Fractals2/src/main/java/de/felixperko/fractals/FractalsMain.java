package de.felixperko.fractals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import de.felixperko.fractals.manager.server.GPUKernelManager;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ClientLocalConnection;
import de.felixperko.fractals.network.interfaces.Messageable;
import de.felixperko.fractals.network.interfaces.ServerLocalMessageable;

public class FractalsMain {
	
	private static final Logger LOG = LoggerFactory.getLogger(FractalsMain.class);
	
	static boolean printLoggerSettings = false;
	static ServerManagers managers;
	
//	public static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	public static int THREAD_COUNT = 4;
	
	public static void main(String[] args) {
		
		printLoggerSettings();
		
		managers = new ServerManagers();
		managers.getSystemManager().registerAvailableSystems();
		managers.getServerNetworkManager().startServerConnectThread(3141);
		managers.getThreadManager().startInputScannerThread();
	}

	public static void printLoggerSettings() {
		if (printLoggerSettings){
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			StatusPrinter.print(lc);
			LOG.info("user.dir: "+System.getProperty("user.dir"));
		}
	}
	
	public static ServerLocalMessageable registerLocalClient(Messageable clientMessageable) {
		ClientLocalConnection clientConnection = managers.getNetworkManager().createNewLocalClient(clientMessageable);
		clientMessageable.setConnection(clientConnection);
		ServerLocalMessageable slm = new ServerLocalMessageable(managers, "IN_LOCAL");
		slm.setClientLocalConnection(clientConnection);
		return slm;
	}
	
	public static void stopInstance(){
		managers.getThreadManager().stopThreads();
	}
	
	public static ServerManagers getManagers(){
		return managers;
	}
}
