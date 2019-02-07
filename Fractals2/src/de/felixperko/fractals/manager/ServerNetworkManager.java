package de.felixperko.fractals.manager;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ClientConnection;
import de.felixperko.fractals.network.ClientLocalConnection;
import de.felixperko.fractals.network.ClientRemoteConnection;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.ServerConnectThread;
import de.felixperko.fractals.network.ServerConnection;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.util.CategoryLogger;

/**
 * Server class.
 * Manages connections to clients.
 */

public class ServerNetworkManager {
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	SenderInfo clientSenderInfo = new SenderInfo(0);
//	ServerConnection serverConnection = null;
	
	ServerConnectThread serverConnectThread;
	
	static int ID_COUNTER = 0;
	
	Map<Integer, ClientConfiguration> clients = new HashMap<>();
	
	Managers managers;
	
	public ServerNetworkManager(Managers managers) {
		this.managers = managers;
	}
	
	public void startServerConnectThread() {
		serverConnectThread = new ServerConnectThread(managers);
		serverConnectThread.start();
	}
	
	public ClientRemoteConnection createNewClient(ServerWriteThread writeThread) {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientRemoteConnection clientConnection = new ClientRemoteConnection(this, info, writeThread);
		clients.put((Integer)clientConnection.getSenderInfo().getClientId(), new ClientConfiguration(clientConnection));
		log.log("new client connected. ID="+info.getClientId());
		return clientConnection;
	}
	
	public void updateClientConfiguration(SenderInfo senderInfo, ClientConfiguration newConfiguration) {
		ClientConfiguration oldConfiguration = clients.get(senderInfo.getClientId());
		if (newConfiguration.getConnection() == null) {
			if (oldConfiguration != null && oldConfiguration.getConnection() != null)
				newConfiguration.setConnection(oldConfiguration.getConnection());
			else
				throw new IllegalArgumentException("No connection for ClientConfiguration found.");
		}
		clients.put(senderInfo.getClientId(), newConfiguration);
		managers.getSystemManager().changedClientConfiguration(oldConfiguration, newConfiguration);
	}
	
	public ClientLocalConnection createNewLocalClient() {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientLocalConnection clientConnection = new ClientLocalConnection(this, info);
		clients.put((Integer)clientConnection.getSenderInfo().getClientId(), new ClientConfiguration(clientConnection));
		return clientConnection;
	}
	
	public ClientConfiguration getClientConfiguration(Integer clientId) {
		return clients.get(clientId);
	}
	
	public ClientConnection getClientConnection(Integer clientId) {
		ClientConfiguration config = getClientConfiguration(clientId);
		if (config == null)
			return null;
		return config.getConnection();
	}
	
	public ClientConnection getClientConnection(SenderInfo senderInfo) {
		return getClientConnection(senderInfo.getClientId());
	}
	
//	public Connection getServerConnection() {
//		return serverConnection;
//	}
	
	public SenderInfo getClientInfo() {
		return clientSenderInfo;
	}

	public void updateChunk(ClientConfiguration client, Chunk chunk) {
		client.getConnection().writeMessage(new ChunkUpdateMessage(chunk));
	}
}
