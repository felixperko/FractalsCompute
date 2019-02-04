package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.net.SocketException;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;

public class ListenThread extends AbstractFractalsThread {
	
	final static CategoryLogger LOGGER_GENERIC = new CategoryLogger("com/server/generic/in", Color.MAGENTA);

	CategoryLogger log = LOGGER_GENERIC;
	
	static int ID_COUNTER = 0;
	
	WriteThread writeThread;
	ObjectInputStream in;
	boolean closeConnection = false;

	public ListenThread(ThreadManager threadManager, WriteThread writeThread, ObjectInputStream in) {
		super(threadManager);
		this.writeThread = writeThread;
		this.in = in;
	}
	
	@Override
	public void run() {
		
		while (!closeConnection && getLifeCycleState() != LifeCycleState.STOPPED) {
			
			while (getLifeCycleState() == LifeCycleState.PAUSED) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			try {
				setLifeCycleState(LifeCycleState.IDLE);
				Message msg = (Message) in.readObject();
				setLifeCycleState(LifeCycleState.RUNNING);
				msg.received(writeThread.getConnection(), log);
			} catch (SocketException e) {
				log.log("lost connection");
				setCloseConnection(true);
			} catch (Exception e) {
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
}
