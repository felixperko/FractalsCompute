package de.felixperko.fractals.network;

import java.io.Serializable;

import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public abstract class Message implements Serializable{
	
	private static final long serialVersionUID = 3353653767834804430L;

	SenderInfo sender;
	
	long sentTime;
	long latency;
	
	long creationTime;
	long lastMessageTime;
	
	protected transient CategoryLogger log;
	
	protected transient Connection connection;
	
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

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public void received(Connection connection, CategoryLogger log) {
		this.latency = System.nanoTime()-sentTime;
		setConnection(connection);
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
		connection.writeMessage(message);
	}
}
