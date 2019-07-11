package de.felixperko.fractals.network.infra;

import java.util.UUID;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.util.CategoryLogger;

public abstract class SystemClientMessage extends ClientMessage{
	
	private static final long serialVersionUID = -1052338656210978218L;
	UUID systemId;
	
	public SystemClientMessage(UUID systemId) {
		super();
		this.systemId = systemId;
	}
	
	public SystemClientMessage(SenderInfo sender, Message<?, ?> lastMessage, UUID systemId) {
		super(sender, lastMessage);
		this.systemId = systemId;
	}
	
	@Override
	public void received(ClientConnection connection, CategoryLogger log) {
		super.received(connection, log);
	}
	
	public UUID getSystemId() {
		return systemId;
	}
}
