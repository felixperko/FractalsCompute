package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.infra.connection.ClientRemoteConnection;
import de.felixperko.fractals.system.systems.infra.LifeCycleComponent;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;

public class ServerConnectThread extends AbstractFractalsThread{

	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	public ServerConnectThread(ServerManagers managers) {
		super(managers, "COM_CONN");
	}
	
	@Override
	public void run() {

		try {
			ServerSocket server = new ServerSocket(3141);
			log.log("Waiting for incoming connections...");
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
				serverWriteThread.setClientConnection(connection);
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
