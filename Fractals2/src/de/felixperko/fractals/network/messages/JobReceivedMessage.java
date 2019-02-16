package de.felixperko.fractals.network.messages;

import java.util.UUID;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.SystemClientMessage;

public class JobReceivedMessage extends SystemClientMessage {

	private static final long serialVersionUID = -4629109684787351923L;

	int[] jobIds;

	public JobReceivedMessage(SenderInfo sender, Message<?, ?> lastMessage, UUID systemId) {
		super(sender, lastMessage, systemId);
	}

	@Override
	protected void process() {
		//TODO job status: assigning -> assigned
	}

}
