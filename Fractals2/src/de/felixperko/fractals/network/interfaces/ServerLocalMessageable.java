package de.felixperko.fractals.network.interfaces;

import java.util.UUID;

import de.felixperko.fractals.data.shareddata.SharedDataController;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.ClientLocalConnection;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.infra.ViewContainerListener;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

/**
 * Interface to server for local connections.
 * setServerConnection() has to be called before writing any messages.
 */
public class ServerLocalMessageable extends AbstractFractalsThread implements Messageable{
	
	public ServerLocalMessageable(Managers managers, String name) {
		super(managers, name);
	}

	CategoryLogger log = new CategoryLogger("com/toLocalServer", new ColorContainer(1f, 0, 1));
	
	ServerConnection serverConnection;
	ClientLocalConnection clientLocalConnection;
	boolean closeConnection;
	
	SharedDataController sharedDataController = new SharedDataController();
	
	public void setClientLocalConnection(ClientLocalConnection clientLocalConnection) {
		this.clientLocalConnection = clientLocalConnection;
	}
	
	@Override
	public Connection<?> getConnection() {
		return serverConnection;
	}

	@Override
	public void setConnection(Connection<?> serverConnection) {
		if (!(serverConnection instanceof ServerConnection))
			throw new IllegalArgumentException("ServerLocalMessageable.setConnection() only accepts ServerConnections");
		this.serverConnection = (ServerConnection) serverConnection;
	}

	@Override
	public void closeConnection() {
		this.closeConnection = true;
		serverConnection.setClosed();
	}

	@Override
	public boolean isCloseConnection() {
		return closeConnection;
	}

	@Override
	public void prepareMessage(Message msg) {
		msg.setSentTime(System.nanoTime());
		msg.setSender(clientLocalConnection.getClientInfo());
	}

	@Override
	public void writeMessage(Message msg) {
		prepareMessage(msg);
		msg.received(clientLocalConnection, log);
	}
	
	@Override
	public void run() {


		setLifeCycleState(LifeCycleState.RUNNING);
		while (!Thread.interrupted()) {
			tick();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				if (!closeConnection)
					e.printStackTrace();
			}
			if (closeConnection)
				break;
		}
		
		setLifeCycleState(LifeCycleState.STOPPED);
	}
	
	protected void tick(){
		sharedDataController.sendMessageIfUpdatesAvailable(getConnection());
	}
	
	public boolean registerViewContainerListener(UUID systemId, ViewContainerListener listener) {
		return ((ServerManagers)managers).getSystemManager().registerViewContainerListener(systemId, listener);
	}
	
	public boolean unregisterViewContainerListener(UUID systemId, ViewContainerListener listener) {
		return ((ServerManagers)managers).getSystemManager().unregisterViewContainerListener(systemId, listener);
	}
}
