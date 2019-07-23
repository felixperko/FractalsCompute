package de.felixperko.fractals.network.infra;

import java.util.UUID;

import de.felixperko.fractals.network.ClientMessageInterface;
import de.felixperko.fractals.network.ClientSystemInterface;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.util.CategoryLogger;

public abstract class SystemServerMessage extends ServerMessage {
	
	private static final long serialVersionUID = 2778751289918945301L;
	
//	ClientSystemInterface clientInterface;
	protected UUID systemId;
	
	public SystemServerMessage(UUID systemId) {
		super();
		this.systemId = systemId;
	}
	
	public SystemServerMessage(SenderInfo sender, Message<?, ?> lastMessage, UUID systemId) {
		super(sender, lastMessage);
		this.systemId = systemId;
	}

//	public ClientMessageInterface getClientMessageInterface() {
//		return clientMessageInterface;
//	}
	
	public ClientSystemInterface getClientSystemInterface() {
		ClientSystemInterface csi = clientMessageInterface.getSystemInterface(systemId);
		if (csi == null)
			throw new IllegalStateException("No system interface found");
		return csi;
	}
}
