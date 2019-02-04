package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.system.systems.infra.LifeCycleComponent;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;

public class ServerConnectThread extends AbstractFractalsThread{

	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	NetworkManager networkManager;
	ThreadManager threadManager;
	
	public ServerConnectThread(NetworkManager networkManager, ThreadManager threadManager) {
		super(threadManager);
		this.networkManager = networkManager;
		this.threadManager = threadManager;
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
				
				//TODO manage write threads to send messages effortlessly
				ServerWriteThread serverWriteThread = threadManager.startServerWriteThread(server.accept());
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ClientRemoteConnection connection = networkManager.createNewClient(serverWriteThread);
				serverWriteThread.setClientConnection(connection);
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
