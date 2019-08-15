package de.felixperko.fractals.manager.common;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.network.ClientWriteThread;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;
import de.felixperko.fractals.network.interfaces.NetworkInterfaceFactory;

public class NetworkManager extends Manager implements INetworkManager{

	protected List<ServerConnection> serverConnections = new ArrayList<>();
	protected Map<ServerConnection, ClientMessageInterface> messageInterfaces = new HashMap<>();
	protected NetworkInterfaceFactory networkInterfaceFactory;
	protected List<ClientWriteThread> writeThreadsToServers = new ArrayList<>();

	public NetworkManager(Managers managers, NetworkInterfaceFactory networkInterfaceFactory) {
		super(managers);
		this.networkInterfaceFactory = networkInterfaceFactory;
	}
	
	@Override
	public ServerConnection connectToServer(String host, int port) {
		Socket socket;
		try {
			socket = new Socket(host, port);
			ServerConnection serverConnection = new ServerConnection(this);
			serverConnections.add(serverConnection);
			ClientMessageInterface clientMessageInterface = networkInterfaceFactory.createMessageInterface(serverConnection);
			messageInterfaces.put(serverConnection, clientMessageInterface);
			ClientWriteThread clientWriteThread = new ClientWriteThread(managers, socket, serverConnection);
			writeThreadsToServers.add(clientWriteThread);
			clientWriteThread.start();
			return serverConnection;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
	
	@Override
	public ClientMessageInterface getMessageInterface(ServerConnection serverConnection) {
		return messageInterfaces.get(serverConnection);
	}

	public void closeServerConnection(ServerConnection serverConnection) {
		serverConnection.getWriteToServer().closeConnection();
		serverConnections.remove(serverConnection);
	}

	@Override
	public List<ServerConnection> getServerConnections() {
		return serverConnections;
	}
	
	public ServerConnection getServerConnection(SenderInfo senderInfo){
		for (ServerConnection conn : serverConnections){
			if (senderInfo.equals(conn.getClientInfo()))
				return conn;
		}
		return null;
	}
}
