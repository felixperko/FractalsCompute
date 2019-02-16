package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.ServerMessage;

public class SessionInitResponseMessage extends ServerMessage{
	
	private static final long serialVersionUID = -6018916737980180526L;

	@Override
	protected void process() {
		// TODO session initiation successful
	}
}
