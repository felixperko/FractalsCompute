package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.net.SocketException;

import de.felixperko.fractals.util.CategoryLogger;

public class ListenThread extends FractalsThread {
	
	final static CategoryLogger LOGGER_GENERIC = new CategoryLogger("com/server/generic/in", Color.MAGENTA);

	CategoryLogger log = LOGGER_GENERIC;
	
	static int ID_COUNTER = 0;
	
	WriteThread writeThread;
	ObjectInputStream in;
	boolean closeConnection = false;

	public ListenThread(WriteThread writeThread, ObjectInputStream in) {
		super("listenThread_"+ID_COUNTER++, 5);
		this.writeThread = writeThread;
		this.in = in;
	}
	
	@Override
	public void run() {

		while (!closeConnection) {
			try {
				setPhase(PHASE_WAITING);
				Message msg = (Message) in.readObject();
				setPhase(PHASE_WORKING);
				msg.received(writeThread.getConnection(), log);
			} catch (SocketException e) {
				log.log("lost connection");
				setCloseConnection(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
