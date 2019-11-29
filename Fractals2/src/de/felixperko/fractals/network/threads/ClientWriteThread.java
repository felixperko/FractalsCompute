package de.felixperko.fractals.network.threads;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.LoggerFactory;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;

public class ClientWriteThread extends WriteThread{
	
	ServerConnection serverConnection;
	
	public ClientWriteThread(Managers managers, Socket socket, ServerConnection serverConnection) throws UnknownHostException, IOException {
		super(managers, socket);
		setConnection(serverConnection);
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
	
	@Override
	public void setConnection(Connection<?> serverConnection) {
		if (!(serverConnection instanceof ServerConnection))
			throw new IllegalArgumentException("ClientWriteThread.setConnection() only accepts Server connections");
		this.serverConnection = (ServerConnection) serverConnection;
		this.serverConnection.setWriteToServer(this);
	}
}
