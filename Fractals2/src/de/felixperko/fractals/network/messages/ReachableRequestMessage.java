package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.infra.Message;

public class ReachableRequestMessage extends Message {

	private static final long serialVersionUID = 7296165361042593042L;

	@Override
	protected void process() {
		answer(new ReachableResponseMessage());
	}

}
