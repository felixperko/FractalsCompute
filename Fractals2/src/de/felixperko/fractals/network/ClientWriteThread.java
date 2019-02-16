package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.manager.ClientManagers;
import de.felixperko.fractals.manager.ClientNetworkManager;
import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.manager.ServerThreadManager;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.util.CategoryLogger;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", Color.MAGENTA);
	
	public ClientWriteThread(ClientManagers managers, Socket socket) throws UnknownHostException, IOException {
		super(managers, socket);
		//TODO managers und clients
		log = superLogger.createSubLogger("out");
		setListenLogger(new CategoryLogger("com/client/in", Color.MAGENTA));
//		setConnection(managers.getClientNetworkManager().getServerConnection());
	}
	
//	@Override
//	public Connection getConnection() {
//		return ((ClientNetworkManager)managers.getNetworkManager()).getServerConnection();
//	}
	
	@Override
	protected void prepareMessage(Message msg) {
		if (msg.getSender() == null) {
			SenderInfo info = ((ClientManagers)managers).getClientNetworkManager().getClientInfo();
			if (info == null)
				throw new IllegalStateException("message can't be adressed");
			msg.setSender(info);
		}
		super.prepareMessage(msg);
	}

	@Override
	public ServerConnection getConnection() {
		return ((ClientManagers)managers).getNetworkManager().getServerConnection();
	}
}
