package de.felixperko.fractals.network.threads;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.infra.connection.ClientRemoteConnection;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;

public class ServerConnectThread extends AbstractFractalsThread{
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerConnectThread.class);
	
	int port;
	
	public ServerConnectThread(ServerManagers managers, int port) {
		super(managers, "COM_CONN");
		this.port = port;
	}
	
	@Override
	public void run() {

		try {
			ServerSocket server = new ServerSocket(port);
			
			LOG.info("Waiting for incoming connections...");
			while (getLifeCycleState() != LifeCycleState.STOPPED) {
				
				while (getLifeCycleState() == LifeCycleState.PAUSED) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				ServerWriteThread serverWriteThread = ((ServerThreadManager) managers.getThreadManager()).startServerWriteThread(server.accept());
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ClientRemoteConnection connection = ((ServerNetworkManager)managers.getNetworkManager()).createNewClient(serverWriteThread);
				serverWriteThread.setConnection(connection);
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
