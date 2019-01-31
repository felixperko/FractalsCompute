package de.felixperko.fractals.network;

import java.awt.Color;

import de.felixperko.fractals.util.CategoryLogger;

public class ClientLocalConnection implements ClientConnection {
	
	SenderInfo senderInfo;
	
	CategoryLogger log = new CategoryLogger("com/local", Color.MAGENTA);
	
	public ClientLocalConnection(SenderInfo localSenderInfo) {
		this.senderInfo = localSenderInfo;
	}

	@Override
	public SenderInfo getSenderInfo() {
		return senderInfo;
	}
	
	@Override
	public void writeMessage(Message msg) {
		msg.setSentTime();
		msg.received(this, log);
	}

}
