package de.felixperko.fractals.network.messages.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.felixperko.fractals.network.infra.SystemClientMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.FractalsTask;

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
		
		for (Entry<Integer, TaskState> e : map.entrySet()) {
			FractalsTask task = getReceiverManagers().getSystemManager().getTask(getSystemId(), e.getKey());
			if (task == null) {
				log.log("warn", "TaskStateChanged: task is null");
			} else {
				task.getStateInfo().setState(e.getValue());
			}
		}
	}

}
