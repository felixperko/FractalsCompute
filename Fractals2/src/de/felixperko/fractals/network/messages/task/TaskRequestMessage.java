package de.felixperko.fractals.network.messages.task;

import de.felixperko.fractals.network.infra.ClientMessage;

public class TaskRequestMessage extends ClientMessage {
	
	private static final long serialVersionUID = 4764129702185799026L;
	
	int amount;

	public TaskRequestMessage(int amount) {
		this.amount = amount;
	}
	
	@Override
	protected void process() {
		// TODO Auto-generated method stub

	}

}
