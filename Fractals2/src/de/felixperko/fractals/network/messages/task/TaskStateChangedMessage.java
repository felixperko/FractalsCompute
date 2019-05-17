package de.felixperko.fractals.network.messages.task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.network.infra.SystemClientMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;

public class TaskStateChangedMessage extends SystemClientMessage {

	private static final long serialVersionUID = -7682669729954693416L;
	
	Map<Integer, TaskState> map = new HashMap<>();

	public TaskStateChangedMessage(UUID systemId, Integer taskId, TaskState state) {
		super(systemId);
		map.put(taskId, state);
	}
	
	public void addEntry(Integer id, TaskState state) {
		map.put(id, state);
	}

	@Override
	protected void process() {
		// TODO Auto-generated method stub
	}

}
