package de.felixperko.fractals.manager;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.network.ClientSystemInterface;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.ServerConnection;

public class ClientNetworkManager extends Manager implements NetworkManager{

	ServerConnection serverConnection = null;
	
	SenderInfo clientSenderInfo = new SenderInfo(0);

	public ClientNetworkManager(Managers managers) {
		super(managers);
	}
	
	public ServerConnection getServerConnection() {
		return serverConnection;
	}
	
	public SenderInfo getClientInfo() {
		return clientSenderInfo;
	}
}
