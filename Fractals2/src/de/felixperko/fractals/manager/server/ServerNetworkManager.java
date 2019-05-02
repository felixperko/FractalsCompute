package de.felixperko.fractals.manager.server;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.ServerConnectThread;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.infra.connection.ClientLocalConnection;
import de.felixperko.fractals.network.infra.connection.ClientRemoteConnection;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.CategoryLogger;

/**
 * Server class.
 * Manages connections to clients.
 */

public class ServerNetworkManager extends Manager implements NetworkManager{
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	ServerConnectThread serverConnectThread;
	
	static int ID_COUNTER = 0;
	
	Map<Integer, ClientConfiguration> clients = new HashMap<>();
	
	ServerManagers managers;
	
	public ServerNetworkManager(ServerManagers managers) {
		super(managers);
		this.managers = managers;
	}
	
	public void startServerConnectThread() {
		serverConnectThread = new ServerConnectThread((ServerManagers) managers);
		serverConnectThread.start();
	}
	
	public ClientRemoteConnection createNewClient(ServerWriteThread writeThread) {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientRemoteConnection clientConnection = new ClientRemoteConnection(this, info, writeThread);
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setConnection(clientConnection);
		clients.put((Integer)clientConnection.getSenderInfo().getClientId(), configuration);
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
		((ServerManagers)managers).getSystemManager().changedClientConfiguration(oldConfiguration, newConfiguration);
	}
	
	public ClientLocalConnection createNewLocalClient() {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientLocalConnection clientConnection = new ClientLocalConnection(this, info);
		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setConnection(clientConnection);
		clients.put((Integer)clientConnection.getSenderInfo().getClientId(), configuration);
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

	public ChunkUpdateMessage updateChunk(ClientConfiguration client, CalcSystem system, AbstractArrayChunk chunk) {
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();
		int upsample = 1;
		if (layer instanceof BreadthFirstUpsampleLayer)
			upsample = ((BreadthFirstUpsampleLayer)layer).getUpsample();
		ChunkUpdateMessage message = new ChunkUpdateMessage(system.getId(), new CompressedChunk((ReducedNaiveChunk) chunk, upsample, chunk.getJobId()), managers.getSystemManager().getStateInfo());
		ClientConnection connection = client.getConnection();
		if (connection != null){
			connection.writeMessage(message);
			return message;
		}
		return null;
	}
	
	public void removeClient(Connection connection) {
		ClientConfiguration conf = clients.remove((Integer)connection.getSenderInfo().getClientId());
		managers.getSystemManager().clientRemoved(conf);
	}
	
	public Map<Integer, ClientConfiguration> getClients(){
		return clients;
	}
}
