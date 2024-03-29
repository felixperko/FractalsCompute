package de.felixperko.fractals.network.threads;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyInputStream;

import de.felixperko.fractals.manager.common.INetworkManager;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;

public class ListenThread extends AbstractFractalsThread {
	
	static int ID_COUNTER = 0;
	private static final Logger LOG = LoggerFactory.getLogger(ListenThread.class);
	
	WriteThread writeThread;
	InputStream in;
	InputStream inComp = null;
	ObjectInputStream inObj = null;
	boolean closeConnection = false;
	
	boolean compressed = false;
	
	int listenThreadId;
	
	List<Runnable> connectionClosedRunnables = new ArrayList<>();

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
					LOG.error("IOException for connection from "+writeThread.getSocket().getInetAddress().getHostAddress());
					e.printStackTrace();
					break mainLoop;
				}
			}
			
			try {
				setLifeCycleState(LifeCycleState.IDLE);
				Message msg = (Message) inObj.readUnshared();
				setLifeCycleState(LifeCycleState.RUNNING);
				INetworkManager net = managers.getNetworkManager();
				msg.received(writeThread.getConnection(), LOG);
				if (writeThread instanceof ServerWriteThread)
					((ServerWriteThread)writeThread).resetLastReachableTime();
			} catch (SocketException | EOFException e) {
				LOG.warn("lost connection");
				setCloseConnection(true);
				if (writeThread.getConnection() instanceof ClientConnection)
					((ServerManagers)managers).getServerNetworkManager().removeClient(writeThread.getConnection());
				connectionClosedRunnables.forEach(runnable -> runnable.run());
			} catch(StreamCorruptedException e) {
				throw new IllegalStateException("Inputstream corrupted");
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
	
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}
	
	public void addConnectionClosedRunnable(Runnable runnable){
		this.connectionClosedRunnables.add(runnable);
	}
	
	public void removeConnectionClosedRunnable(Runnable runnable){
		this.connectionClosedRunnables.remove(runnable);
	}
}
