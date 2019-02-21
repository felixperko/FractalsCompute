package de.felixperko.fractals.network.messages.task;

import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;

public class TaskStateChangedMessage extends ClientMessage {

	private static final long serialVersionUID = -7682669729954693416L;
	
	Integer id;
	TaskState state;

	public TaskStateChangedMessage(Integer id, TaskState state) {
		this.id = id;
		this.state = state;
	}
	

	@Override
	protected void process() {
		// TODO Auto-generated method stub

	}

}
