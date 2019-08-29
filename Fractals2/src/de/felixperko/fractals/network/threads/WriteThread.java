package de.felixperko.fractals.network.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import de.felixperko.fractals.data.shareddata.SharedDataController;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.network.ComAdapter;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;
import de.felixperko.fractals.util.CategoryLogger;

public abstract class WriteThread extends AbstractFractalsThread implements ComAdapter {
	
	static int ID_COUNTER = 0;
	
	CategoryLogger log;
	
	ListenThread listenThread;
	boolean closeConnection = false;
	
	Queue<Message> pendingMessages = new PriorityQueue<>();
	Queue<Message> newMessages = new LinkedList<>();
	private ObjectOutputStream out;
	private SnappyOutputStream outComp;
	protected Socket socket;
	
	boolean compression = false;
	
//	Connection connection;
	
	private CategoryLogger listenLogger; //used to buffer logger for listen thread until its creation
	
	int writeThreadId;
	
	SharedDataController sharedDataController = new SharedDataController();
	
	public WriteThread(Managers managers, Socket socket) {
		super(managers, "COM_"+ID_COUNTER+"_OUT");
		this.writeThreadId = ID_COUNTER++;
		this.socket = socket;
	}

	public void run() {
		try {
			InputStream inStream = socket.getInputStream();
			if (compression) {
				outComp = new SnappyOutputStream(socket.getOutputStream());
				out = new ObjectOutputStream(outComp);
			} else
				out = new ObjectOutputStream(socket.getOutputStream());
			listenThread = new ListenThread(managers, this, inStream);
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
				
				while (newMessages.isEmpty() && pendingMessages.isEmpty()) {
					tick();
					setLifeCycleState(LifeCycleState.IDLE);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (!closeConnection)
							e.printStackTrace();
					}
					if (closeConnection)
						break mainLoop;
				}
				
				setLifeCycleState(LifeCycleState.RUNNING);
				
				synchronized(newMessages) {
					pendingMessages.addAll(newMessages);
					newMessages.clear();
				}
				
				try {
					synchronized (pendingMessages) {
						Iterator<Message> it = pendingMessages.iterator();
	//					while (!pendingMessages.isEmpty()) {
	//						Message msg = pendingMessages.poll();
						if (it.hasNext()) {
	//						if (closeConnection)
	//							break mainLoop;
							Message msg = it.next();
							it.remove();
							msg.executeSentCallbacks();
							msg.setSent(true);
							if (msg.isCancelled())
								continue;
							prepareMessage(msg);
							log.log("sending message: "+msg.getClass().getSimpleName());
							try {
								out.writeUnshared(msg);
								out.flush();
								if (outComp != null)
									outComp.flush();
								out.reset();
							} catch (SocketException e) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
								if (!closeConnection)
									throw e;
							}
						}
					}
				} catch (Exception e){
					if (closeConnection)
						break mainLoop;
					throw e;
				}
			}
			
//			try {
//				inComp.close();
//			} catch (SocketException e) {if (!closeConnection) throw e;}
			try {
				out.close();
			} catch (SocketException e) {if (!closeConnection) throw e;}
			try {
				socket.close();
			} catch (SocketException e) {if (!closeConnection) throw e;}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void prepareMessage(Message msg) {
		msg.setSentTime();
	}

	protected void tick() {
		sharedDataController.sendMessageIfUpdatesAvailable(getConnection());
	}

	/* (non-Javadoc)
	 * @see de.felixperko.fractals.network.threads.ComAdapter#writeMessage(de.felixperko.fractals.network.infra.Message)
	 */
	@Override
	public void writeMessage(Message msg) {
		synchronized (newMessages) {
			newMessages.add(msg);
		}
	}
	
	@Override
	public void closeConnection() {
		closeConnection = true;
		getConnection().setClosed();
		if (getLifeCycleState() == LifeCycleState.PAUSED)
			continueThread();
	}

	@Override
	public boolean isCloseConnection() {
		return closeConnection;
	};
	
	public void setListenLogger(CategoryLogger log) {
		listenLogger = log;
		if (listenThread != null)
			listenThread.setLogger(listenLogger);
	}

	@Override
	public abstract Connection<?> getConnection();
	
	public void setCompression(boolean compression) {
		if (this.compression != compression) {
			try {
				if (compression) {
					if (out != null)
						out.close();
					
					outComp = new SnappyOutputStream(socket.getOutputStream());
					out = new ObjectOutputStream(outComp);
				} else {
					if (outComp != null)
						outComp.close();
					if (out != null)
						out.close();
					
					out = new ObjectOutputStream(socket.getOutputStream());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.compression = compression;
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
