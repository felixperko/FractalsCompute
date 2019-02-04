package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.util.CategoryLogger;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", Color.MAGENTA);
	
	NetworkManager networkManager;
	
	public ClientWriteThread(ThreadManager threadManager, NetworkManager networkManager, Socket socket) throws UnknownHostException, IOException {
		super(threadManager, socket);
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
