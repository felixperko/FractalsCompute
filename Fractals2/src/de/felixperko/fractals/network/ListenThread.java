package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;

import org.xerial.snappy.SnappyInputStream;

import de.felixperko.fractals.manager.client.ClientManagers;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;

public class ListenThread extends AbstractFractalsThread {
	
	final static CategoryLogger LOGGER_GENERIC = new CategoryLogger("com/server/generic/in", ColorContainer.MAGENTA);
	
	static int ID_COUNTER = 0;

	CategoryLogger log = LOGGER_GENERIC;
	
	WriteThread writeThread;
	InputStream in;
	InputStream inComp = null;
	ObjectInputStream inObj = null;
	boolean closeConnection = false;
	
	boolean compressed = false;
	
	int listenThreadId;

	public ListenThread(Managers managers, WriteThread writeThread, InputStream in) {
		super(managers, "COM_"+ID_COUNTER+"_IN");
		listenThreadId = ID_COUNTER++;
		this.writeThread = writeThread;
		this.in = in;
	}

	@Override
	public void run() {
		
		mainLoop:
		while (!closeConnection && getLifeCycleState() != LifeCycleState.STOPPED) {
			
			while (getLifeCycleState() == LifeCycleState.PAUSED) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			if (inObj == null) {
				try {
					if (compressed) {
						inComp = new SnappyInputStream(in);
						inObj = new ObjectInputStream(inComp);
					} else {
						inObj = new ObjectInputStream(in);
					}
				} catch (IOException e) {
					if (closeConnection)
						break mainLoop;
					e.printStackTrace();
				}
			}
			
			try {
				setLifeCycleState(LifeCycleState.IDLE);
				Message msg = (Message) inObj.readUnshared();
				setLifeCycleState(LifeCycleState.RUNNING);
				NetworkManager net = managers.getNetworkManager();
				msg.received(writeThread.getConnection(), log);
				if (writeThread instanceof ServerWriteThread)
					((ServerWriteThread)writeThread).resetLastReachableTime();
			} catch (SocketException e) {
				log.log("lost connection");
				setCloseConnection(true);
				if (managers instanceof ServerManagers)
					((ServerManagers)managers).getServerNetworkManager().removeClient(writeThread.getConnection());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (inComp != null) {
			try {
				inComp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (inObj != null) {
			try {
				inObj.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		setLifeCycleState(LifeCycleState.STOPPED);
	}

	public boolean isCloseConnection() {
		return closeConnection;
	}

	public void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
		writeThread.closeConnection();
	}

	public void setLogger(CategoryLogger log) {
		this.log = log;
	}
	
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}
}
