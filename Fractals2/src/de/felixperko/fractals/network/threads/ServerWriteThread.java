package de.felixperko.fractals.network.threads;

import java.net.Socket;

import org.slf4j.LoggerFactory;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ClientRemoteConnection;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.network.messages.ConnectedMessage;
import de.felixperko.fractals.network.messages.ReachableRequestMessage;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;
import de.felixperko.fractals.util.NumberUtil;

public class ServerWriteThread extends WriteThread {
	
	//final static CategoryLogger superLog = new CategoryLogger("com/server", ColorContainer.MAGENTA);
	
	ClientRemoteConnection clientConnection;
	
	long reachableRequestInterval = (long) (0.5/NumberUtil.NS_TO_S);
	long lastReachableTime;
	
	public ServerWriteThread(ServerManagers managers, Socket socket) {
		super(managers, socket);
		lastReachableTime = System.nanoTime();
	}
	
	@Override
	protected void tick() {
		if (System.nanoTime() - lastReachableTime > reachableRequestInterval) {
			lastReachableTime = System.nanoTime();
			writeMessage(new ReachableRequestMessage(clientConnection, ((ServerManagers)managers).getSystemManager().getStateInfo()));
		}
	}

	@Override
	public ClientRemoteConnection getConnection() {
		return clientConnection;
	}
	
	public void resetLastReachableTime() {
		this.lastReachableTime = System.nanoTime();
	}

	@Override
	public void setConnection(Connection<?> clientRemoteConnection) {
		if (!(clientRemoteConnection instanceof ClientRemoteConnection))
			throw new IllegalArgumentException("ServerWriteThread.setConnection() only accepts ClientRemoteConnections");
		this.clientConnection = (ClientRemoteConnection)clientRemoteConnection;
//		setListenLogger(superLog.createSubLogger(clientConnection.getSenderInfo().getClientId()+"/in"));
//		this.log = superLog.createSubLogger(clientConnection.getSenderInfo().getClientId()+"/out");
		setListenLogger(LoggerFactory.getLogger(clientConnection.getSenderInfo().getClientId()+"/in"));
		this.log = LoggerFactory.getLogger(clientConnection.getSenderInfo().getClientId()+"/out");
		writeMessage(new ConnectedMessage(clientConnection));
	}
}
