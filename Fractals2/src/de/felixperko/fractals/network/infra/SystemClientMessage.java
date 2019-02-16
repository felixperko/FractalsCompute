package de.felixperko.fractals.network.infra;

import java.util.UUID;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.util.CategoryLogger;

public abstract class SystemClientMessage extends ClientMessage{
	
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
		// TODO Auto-generated method stub
		super.received(connection, log);
	}
}
