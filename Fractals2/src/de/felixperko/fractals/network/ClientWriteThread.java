package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerNetworkManager;
import de.felixperko.fractals.manager.ThreadManager;
import de.felixperko.fractals.util.CategoryLogger;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", Color.MAGENTA);
	
	ClientNetworkManager networkManager;
	
	public ClientWriteThread(Managers managers, ClientNetworkManager networkManager, Socket socket) throws UnknownHostException, IOException {
		super(managers, socket);
		log = superLogger.createSubLogger("out");
		setListenLogger(new CategoryLogger("com/client/in", Color.MAGENTA));
		this.networkManager = networkManager;
		setConnection(networkManager.getServerConnection());
	}
	
	@Override
	protected void prepareMessage(Message msg) {
		if (msg.getSender() == null) {
			SenderInfo info = networkManager.getClientInfo();
			if (info == null)
				throw new IllegalStateException("message can't be adressed");
			msg.setSender(info);
		}
		super.prepareMessage(msg);
	}
}
