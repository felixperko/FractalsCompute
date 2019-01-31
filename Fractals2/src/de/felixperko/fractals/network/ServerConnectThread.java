package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;

import de.felixperko.fractals.util.CategoryLogger;

public class ServerConnectThread extends FractalsThread{

	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	NetworkManager networkManager;
	
	public ServerConnectThread() {
		super("server_con", 5);
		networkManager = FractalsServerMain.networkManager;
	}
	
	@Override
	public void run() {

		try {
			ServerSocket server = new ServerSocket(3141);
			log.log("Waiting for incoming connections...");
			while (!Thread.interrupted()) {
				//TODO manage write threads to send messages effortlessly
				ServerWriteThread serverWriteThread = FractalsMain.threadManager.startServerSocket(server.accept());
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
