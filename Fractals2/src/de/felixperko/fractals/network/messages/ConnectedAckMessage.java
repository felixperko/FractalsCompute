package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.ServerMessage;

public class ConnectedAckMessage extends ClientMessage{

	private static final long serialVersionUID = 4746386962938401924L;

	@Override
	protected void process() {
		log.log("Got answer from client!");
	}
	
}
