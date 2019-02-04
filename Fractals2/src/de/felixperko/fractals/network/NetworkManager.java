package de.felixperko.fractals.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.util.CategoryLogger;

/**
 * Server class.
 * Manages connections to clients.
 */

public class NetworkManager {
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	SenderInfo clientSenderInfo = new SenderInfo(0);
	ServerConnection serverConnection = null;
	
	ServerConnectThread serverConnectThread;
	
	static int ID_COUNTER = 0;
	
	Map<Integer, ClientConnection> clients = new HashMap<>();
	
	ThreadManager threadManager;
	
	public NetworkManager(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}
	
	public void startServerConnectThread() {
		serverConnectThread = new ServerConnectThread(this, threadManager);
		serverConnectThread.start();
	}
	
	public ClientRemoteConnection createNewClient(ServerWriteThread writeThread) {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientRemoteConnection clientConnection = new ClientRemoteConnection(this, info, writeThread);
		clients.put((Integer)clientConnection.getSenderInfo().clientId, clientConnection);
		log.log("new client connected. ID="+info.clientId);
		return clientConnection;
	}
	
	public ClientLocalConnection createNewLocalClient() {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientLocalConnection clientConnection = new ClientLocalConnection(this, info);
		clients.put((Integer)clientConnection.getSenderInfo().clientId, clientConnection);
		return clientConnection;
	}
	
	public ClientConnection getClientConnection(Integer clientId) {
		return clients.get(clientId);
	}
	
	public ClientConnection getClientConnection(SenderInfo senderInfo) {
		return getClientConnection(senderInfo.clientId);
	}
	
	public Connection getServerConnection() {
		return serverConnection;
	}
	

	public SenderInfo getClientInfo() {
		return clientSenderInfo;
	}
}
