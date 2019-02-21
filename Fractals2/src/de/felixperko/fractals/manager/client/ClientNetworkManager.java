package de.felixperko.fractals.manager.client;

import java.io.IOException;
import java.net.Socket;

import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.ClientMessageInterface;
import de.felixperko.fractals.network.ClientWriteThread;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ServerConnection;

public class ClientNetworkManager extends Manager implements NetworkManager{

	ServerConnection serverConnection = null;
	
	SenderInfo clientSenderInfo = null;
	
	ClientMessageInterface clientMessageInterface;
	
	ClientWriteThread clientWriteThread;

	public ClientNetworkManager(ClientManagers managers, ClientMessageInterface clientMessageInterface) {
		super(managers);
		this.clientMessageInterface = clientMessageInterface;
		serverConnection = new ServerConnection(this);
	}
	
	public void connectToServer(String host, int port) {
		Socket socket;
		try {
			socket = new Socket(host, port);
			clientWriteThread = new ClientWriteThread((ClientManagers)managers, socket);
			clientWriteThread.start();
			serverConnection.setWriteToServer(clientWriteThread);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public ServerConnection getServerConnection() {
		return serverConnection;
	}
	
	public SenderInfo getClientInfo() {
		return clientSenderInfo;
	}
	
	public void setClientInfo(SenderInfo clientInfo) {
		this.clientSenderInfo = clientInfo;
	}

	public ClientMessageInterface getMessageInterface() {
		return clientMessageInterface;
	}
}
