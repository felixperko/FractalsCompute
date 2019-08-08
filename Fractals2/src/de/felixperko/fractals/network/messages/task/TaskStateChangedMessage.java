package de.felixperko.fractals.network.messages.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.network.infra.SystemClientMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.FractalsTask;

public class TaskStateChangedMessage extends ClientMessage {

	private static final long serialVersionUID = -7682669729954693416L;
	
	List<TaskStateInfo> taskStateInfos = new ArrayList<>();

	public TaskStateChangedMessage(TaskStateInfo stateInfo) {
		taskStateInfos.add(stateInfo);
	}
	
	public void addEntry(TaskStateInfo stateInfo) {
		taskStateInfos.add(stateInfo);
	}

	@Override
	protected void process() {
		
		for (TaskStateInfo taskStateInfo : taskStateInfos) {
			FractalsTask task = getReceiverManagers().getSystemManager().getTask(taskStateInfo.getSystemId(), taskStateInfo.getTaskId());
			if (task == null) {
				log.log("warn", "TaskStateChanged: task is null");
			} else {
				task.setStateInfo(taskStateInfo);
			}
		}
	}

}
