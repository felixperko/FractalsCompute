package de.felixperko.fractals.network.messages.task;

import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.system.task.FractalsTask;

public class TaskFinishedMessage extends ClientMessage {

	private static final long serialVersionUID = -5817288498408381961L;
	
	FractalsTask task;

	public TaskFinishedMessage(FractalsTask task) {
		this.task = task;
	}

	@Override
	protected void process() {
		// TODO Auto-generated method stub

	}

}
