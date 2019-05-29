package de.felixperko.fractals.network.infra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.network.Connection;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public abstract class Message<CONN extends Connection, BACKCONN extends Connection> implements Serializable{
	
	private static final long serialVersionUID = 3353653767834804430L;

	SenderInfo sender;
	
	long sentTime;
	long latency;
	
	long creationTime;
	long lastMessageTime;
	
	transient boolean cancelled = false;
	
	protected transient CategoryLogger log;
	
	protected transient List<Runnable> sentCallbacks = null;
	
	boolean sent = false;
	
	public Message() {
		
	}
	
	public Message(SenderInfo sender, Message lastMessage) {
		this.sender = sender;
		this.creationTime = System.nanoTime();
		if (lastMessage != null)
			setLastMessageTime(lastMessage.getLatency());
	}
	
	void setComLogger(CategoryLogger comLogger) {
		log = comLogger;
	}

	protected abstract void setConnection(CONN connection);
	public abstract CONN getConnection();
	
	protected abstract void setBackConnection(BACKCONN connection);
	public abstract BACKCONN getBackConnection();
	
	public void received(BACKCONN connection, CategoryLogger log) {
		this.latency = System.nanoTime()-sentTime;
		if (connection == null)
			throw new IllegalStateException("Back connection is null");
//		log.log("received message: "+this);
		setBackConnection(connection);
		setComLogger(log);
		logIncoming();
		process();
	}
	
	protected abstract void process();

	public long getLatency() {
		return latency;
	}
	
	public void logIncoming() {
		log.log("received "+getClass().getSimpleName()+" ("+getLatencyInMs(1)+"ms)");
	}
	
	public double getLatencyInMs(int precision) {
		return NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS * getLatency(), precision);
	}
	
	public SenderInfo getSender() {
		return sender;
	}

	public void setSender(SenderInfo sender) {
		this.sender = sender;
	}

	public long getSentTime() {
		return sentTime;
	}

	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}

	public void setSentTime() {
		setSentTime(System.nanoTime());
	}

	public long getLastMessageTime() {
		return lastMessageTime;
	}

	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
	
	protected void answer(Message message) {
		getBackConnection().writeMessage(message);
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public void addSentCallback(Runnable run) {
		if (sentCallbacks == null) {
			sentCallbacks = new ArrayList<>();
		}
		sentCallbacks.add(run);
	}

	public void executeSentCallbacks() {
		if (sentCallbacks != null)
			for (Runnable run : sentCallbacks)
				run.run();
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}
	
	public Managers getReceiverManagers() {
		return getBackConnection().getNetworkManager().getManagers();
	}
}
