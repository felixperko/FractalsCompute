package de.felixperko.fractals.network.messages;

import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.SystemServerMessage;

public class SystemConnectedMessage extends SystemServerMessage {

	private static final long serialVersionUID = 1578093945175083508L;
	
	ClientConfiguration clientConfiguration;
	
	public SystemConnectedMessage(UUID systemId, ClientConfiguration clientConfiguration) {
		super(systemId);
		this.clientConfiguration = clientConfiguration;
	}

	@Override
	protected void process() {
		getClientMessageInterface().createdSystem(systemId, clientConfiguration);
	}

}
