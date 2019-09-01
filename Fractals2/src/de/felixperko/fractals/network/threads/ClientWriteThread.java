package de.felixperko.fractals.network.threads;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", ColorContainer.MAGENTA);
	
	ServerConnection serverConnection;
	
	public ClientWriteThread(Managers managers, Socket socket, ServerConnection serverConnection) throws UnknownHostException, IOException {
		super(managers, socket);
		this.serverConnection = serverConnection;
		this.serverConnection.setWriteToServer(this);
		log = superLogger.createSubLogger("out");
		setListenLogger(new CategoryLogger("com/client/in", ColorContainer.MAGENTA));
	}
	
	@Override
	public void prepareMessage(Message msg) {
		if (msg.getSender() == null) {
			SenderInfo info = serverConnection.getClientInfo();
			if (info == null)
				throw new IllegalStateException("message can't be adressed");
			msg.setSender(info);
		}
		super.prepareMessage(msg);
	}

	@Override
	public ServerConnection getConnection() {
		return serverConnection;
	}
}
