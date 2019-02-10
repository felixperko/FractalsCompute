package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.Message;

public class JobReceivedMessage extends Message {

	private static final long serialVersionUID = -4629109684787351923L;

	int[] jobIds;
	
	public JobReceivedMessage() {
	}

	public JobReceivedMessage(SenderInfo sender, Message lastMessage) {
		super(sender, lastMessage);
	}

	@Override
	protected void process() {
		//TODO job status: assigning -> assigned
	}

}
