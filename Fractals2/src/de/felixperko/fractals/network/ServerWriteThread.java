package de.felixperko.fractals.network;

import java.awt.Color;
import java.net.Socket;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.ClientRemoteConnection;
import de.felixperko.fractals.network.messages.ConnectedMessage;
import de.felixperko.fractals.network.messages.ReachableRequestMessage;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public class ServerWriteThread extends WriteThread {
	
	final static CategoryLogger superLog = new CategoryLogger("com/server", Color.MAGENTA);
	
	ClientRemoteConnection clientConnection;
	
	long reachableRequestInterval = (long) (5/NumberUtil.NS_TO_S);
	long lastReachableTime;
	
	public ServerWriteThread(ServerManagers managers, Socket socket) {
		super(managers, socket);
		lastReachableTime = System.nanoTime();
	}
	
	@Override
	public void writeMessage(Message msg) {
		//TODO seems to be wrong (messages from server don't have a SenderInfo, right?)
//		if (clientConnection != null) {
//			if (msg.getSender() == null)
//				msg.setSender(clientConnection.getSenderInfo());
//			else if (!msg.getSender().equals(clientConnection.getSenderInfo())){
//				throw new IllegalStateException("Wrong thread to send message to client");
//			}
//		}
		super.writeMessage(msg);
	}
	
	@Override
	protected void tick() {
		if (System.nanoTime() - lastReachableTime > reachableRequestInterval) {
			lastReachableTime = System.nanoTime();
			writeMessage(new ReachableRequestMessage());
		}
	}

	public void setClientConnection(ClientRemoteConnection clientConnection) {
		this.clientConnection = clientConnection;
		setListenLogger(superLog.createSubLogger(clientConnection.getSenderInfo().getClientId()+"/in"));
		this.log = superLog.createSubLogger(clientConnection.getSenderInfo().getClientId()+"/out");
		writeMessage(new ConnectedMessage(clientConnection));
	}

	@Override
	public ClientRemoteConnection getConnection() {
		return clientConnection;
	}
	
	public void resetLastReachableTime() {
		this.lastReachableTime = System.nanoTime();
	}
}
