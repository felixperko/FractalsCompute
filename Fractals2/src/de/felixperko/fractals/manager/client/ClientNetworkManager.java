package de.felixperko.fractals.manager.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.ClientWriteThread;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;
import de.felixperko.fractals.network.interfaces.NetworkInterfaceFactory;

public class ClientNetworkManager extends Manager implements NetworkManager{

	List<ServerConnection> serverConnections = new ArrayList<>();
	Map<ServerConnection, ClientMessageInterface> clientMessageInterfaces = new HashMap<>();
	NetworkInterfaceFactory networkInterfaceFactory;
	
	ClientWriteThread clientWriteThread;

	public ClientNetworkManager(ClientManagers managers, NetworkInterfaceFactory networkInterfaceFactory) {
		super(managers);
		this.networkInterfaceFactory = networkInterfaceFactory;
	}
	
	public void connectToServer(String host, int port) {
		Socket socket;
		try {
			socket = new Socket(host, port);
			ServerConnection serverConnection = new ServerConnection(this);
			serverConnections.add(serverConnection);
			ClientMessageInterface clientMessageInterface = networkInterfaceFactory.createMessageInterface(serverConnection);
			clientMessageInterfaces.put(serverConnection, clientMessageInterface);
			clientWriteThread = new ClientWriteThread((ClientManagers)managers, socket, serverConnection);
			clientWriteThread.start();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public ClientMessageInterface getMessageInterface(ServerConnection serverConnection) {
		return clientMessageInterfaces.get(serverConnection);
	}

	public void closeServerConnection(ServerConnection serverConnection) {
		serverConnection.getWriteToServer().closeConnection();
		serverConnections.remove(serverConnection);
	}

	@Override
	public List<ServerConnection> getServerConnections() {
		return serverConnections;
	}
}
