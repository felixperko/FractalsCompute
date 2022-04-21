package de.felixperko.fractals.network.messages;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.network.infra.ServerMessage;

public class SessionInitResponseMessage extends ServerMessage{
	
	private static final long serialVersionUID = -6018916737980180526L;

	@Override
	protected void process() {
		ResourceRequestMessage resourceRequestMessage = getClientMessageInterface().requestResources();
		if (resourceRequestMessage != null)
			answer(resourceRequestMessage);
	}
}
