package de.felixperko.fractals.network;

public interface Connection {
	public void writeMessage(Message msg);

	public SenderInfo getSenderInfo();
}
