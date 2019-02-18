package de.felixperko.fractals.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.manager.ServerThreadManager;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;

public abstract class WriteThread extends AbstractFractalsThread {
	
	static int ID_COUNTER = 0;
	
	CategoryLogger log;
	
	ListenThread listenThread;
	boolean closeConnection = false;
	
	Queue<Message> pendingMessages = new LinkedList<>();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	protected Socket socket;
	
//	Connection connection;
	
	private CategoryLogger listenLogger; //used to buffer logger for listen thread until its creation
	
	int writeThreadId;
	
	public WriteThread(Managers managers, Socket socket) {
		super(managers, "COM_"+ID_COUNTER+"_OUT");
		this.writeThreadId = ID_COUNTER++;
		this.socket = socket;
	}

	public void run() {
		try {
			InputStream inStream = socket.getInputStream();
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(inStream);
			listenThread = new ListenThread(managers, this, in);
			if (listenLogger != null)
				listenThread.setLogger(listenLogger);
			listenThread.start();
			
			mainLoop:
			while (getLifeCycleState() != LifeCycleState.STOPPED) {
				
				while (getLifeCycleState() == LifeCycleState.PAUSED) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				while (pendingMessages.isEmpty()) {
					tick();
					setLifeCycleState(LifeCycleState.IDLE);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (!closeConnection)
							e.printStackTrace();
					}
				}
				
				setLifeCycleState(LifeCycleState.RUNNING);
				synchronized(this) {
					Iterator<Message> it = pendingMessages.iterator();
					while (it.hasNext()) {
						if (closeConnection)
							break mainLoop;
						Message msg = it.next();
						prepareMessage(msg);
						log.log("sending message: "+msg.getClass().getSimpleName());
						out.writeObject(msg);
						it.remove();
					}
				}
				out.flush();
			}
			
			in.close();
			out.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void prepareMessage(Message msg) {
		msg.setSentTime();
	}

	protected void tick() {
	}

	public synchronized void writeMessage(Message msg) {
		pendingMessages.add(msg);
	}
	
	public void closeConnection() {
		closeConnection = true;
		if (getLifeCycleState() == LifeCycleState.PAUSED)
			continueThread();
	}

	public boolean isCloseConnection() {
		return closeConnection;
	};
	
	public void setListenLogger(CategoryLogger log) {
		if (listenThread == null)
			listenLogger = log;
		else
			listenThread.setLogger(listenLogger);
	}

	public abstract Connection getConnection();
	
//	public void setConnection(Connection connection) {
//		this.connection = connection;
//	}
	
//	public Connection getConnection() {
//		return connection;
//	}
}
