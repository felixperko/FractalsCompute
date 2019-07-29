package de.felixperko.fractals.manager.common;

import java.util.List;

import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;

public interface INetworkManager {

	public Managers getManagers();

	public ClientMessageInterface getMessageInterface(ServerConnection serverConnection);
	
	public List<ServerConnection> getServerConnections();
	
	public ServerConnection connectToServer(String host, int port);
}
