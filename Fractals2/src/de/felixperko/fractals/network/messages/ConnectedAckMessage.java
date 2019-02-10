package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.ServerMessage;

public class ConnectedAckMessage extends ServerMessage{

	private static final long serialVersionUID = 4746386962938401924L;

	@Override
	protected void process() {
		log.log("Got answer from client!");
	}
	
}
