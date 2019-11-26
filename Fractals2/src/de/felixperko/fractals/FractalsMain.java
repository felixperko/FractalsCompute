package de.felixperko.fractals;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ClientLocalConnection;
import de.felixperko.fractals.network.interfaces.Messageable;
import de.felixperko.fractals.network.interfaces.ServerLocalMessageable;

public class FractalsMain {
	
	static ServerManagers managers;
	
	public static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	public static int THREAD_COUNT = 2;
	
	public static void main(String[] args) {
//		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//		StatusPrinter.print(lc);
		
		System.out.println(System.getProperty("user.dir"));
		managers = new ServerManagers();

		managers.getSystemManager().insertAvailableSystems();
		
		managers.getThreadManager().startWorkerThreads(THREAD_COUNT, false);
		managers.getServerNetworkManager().startServerConnectThread();
		
		managers.getThreadManager().startInputScannerThread();
	}
	
	public static ServerLocalMessageable registerLocalClient(Messageable clientMessageable) {
		ClientLocalConnection clientConnection = managers.getNetworkManager().createNewLocalClient(clientMessageable);
		clientMessageable.setConnection(clientConnection);
		ServerLocalMessageable slm = new ServerLocalMessageable(managers, "IN_LOCAL");
		slm.setClientLocalConnection(clientConnection);
		return slm;
	}
}
