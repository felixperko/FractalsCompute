package de.felixperko.fractals.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.util.CategoryLogger;

/**
 * Server class.
 * Manages connections to clients.
 */

public class NetworkManager {
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	static int ID_COUNTER = 0;
	
	Map<Integer, ClientConnection> clients = new HashMap<>();
	
	public NetworkManager() {
	}
	
	public ClientRemoteConnection createNewClient(ServerWriteThread writeThread) {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientRemoteConnection clientConnection = new ClientRemoteConnection(info, writeThread);
		clients.put((Integer)clientConnection.getSenderInfo().clientId, clientConnection);
		log.log("new client connected. ID="+info.clientId);
		return clientConnection;
	}
	
	public ClientLocalConnection createNewLocalClient() {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientLocalConnection clientConnection = new ClientLocalConnection(info);
		clients.put((Integer)clientConnection.getSenderInfo().clientId, clientConnection);
		return clientConnection;
	}
	
	public ClientConnection getConnection(Integer clientId) {
		return clients.get(clientId);
	}
	
	public ClientConnection getConnection(SenderInfo senderInfo) {
		return getConnection(senderInfo.clientId);
	}
}
