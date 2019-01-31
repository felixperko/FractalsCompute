package de.felixperko.fractals.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import de.felixperko.fractals.util.CategoryLogger;

public class WriteThread extends FractalsThread {
	
	static int ID_COUNTER = 0;
	
	CategoryLogger log;
	
	ListenThread listenThread;
	boolean closeConnection = false;
	
	Queue<Message> pendingMessages = new LinkedList<>();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	protected Socket socket;
	
	Connection connection;
	
	private CategoryLogger listenLogger; //used to buffer logger for listen thread until its creation
	
	public WriteThread(Socket socket) {
		super("writeThread_"+ID_COUNTER++, 5);
		this.socket = socket;
	}

	public void run() {
		try {
			InputStream inStream = socket.getInputStream();
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(inStream);
			listenThread = new ListenThread(this, in);
			if (listenLogger != null)
				listenThread.setLogger(listenLogger);
			listenThread.start();
			
			while (!Thread.interrupted()) {
				
				while (pendingMessages.isEmpty() && !Thread.interrupted()) {
					tick();
					setPhase(PHASE_IDLE);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (!closeConnection)
							e.printStackTrace();
					}
				}
				
				setPhase(PHASE_WORKING);
				Iterator<Message> it = pendingMessages.iterator();
				while (it.hasNext()) {
					if (closeConnection)
						break;
					Message msg = it.next();
					prepareMessage(msg);
					out.writeObject(msg);
					it.remove();
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

	public void writeMessage(Message msg) {
		pendingMessages.add(msg);
	}
	
	public void closeConnection() {
		closeConnection = true;
		interrupt();
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
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public Connection getConnection() {
		return connection;
	}
}
