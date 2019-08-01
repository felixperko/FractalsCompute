package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import de.felixperko.fractals.data.shareddata.MappedSharedDataUpdate;

public class SystemStateInfo implements Serializable{
	
	private static final long serialVersionUID = -2682394717324577351L;
	
//	int stageCount;
//	int currentWorkerThreads;
	Map<Integer, TaskStateInfo> taskStates = new ConcurrentHashMap<>();
	
	ServerStateInfo serverStateInfo;
	
	UUID systemId;
	
	public SystemStateInfo(UUID id) {
		this.systemId = id;
	}

	public void addTaskStateInfo(TaskStateInfo taskStateInfo) {
		taskStateInfo.setSystemStateInfo(this);
		taskStates.put((Integer)taskStateInfo.getTaskId(), taskStateInfo);
		
		getTaskListForState(taskStateInfo.getState()).add(taskStateInfo);
		updateTime();
	}
	
	public List<TaskStateInfo> getTaskListForState(TaskState state){
		List<TaskStateInfo> stateList = new ArrayList<>();
		for (TaskStateInfo info : taskStates.values())
			if (info.getState().equals(state))
				stateList.add(info);
		return stateList;
	}
	
	public TaskStateInfo getTaskStateInfo(Integer taskId) {
		return taskStates.get(taskId);
	}

	public ServerStateInfo getServerStateInfo() {
		return serverStateInfo;
	}

	protected void setServerStateInfo(ServerStateInfo serverStateInfo) {
		this.serverStateInfo = serverStateInfo;
	}
	
	protected void updateTime(){
		serverStateInfo.updateTime();
	}
	
	public long getUpdateTime(){
		return serverStateInfo.getUpdateTime();
	}

	
	public TaskStateUpdate taskStateChanged(int taskId, TaskState oldState, TaskStateInfo stateInfo) {
		if (stateInfo.getState() != oldState) {
			getTaskListForState(oldState).remove(stateInfo);
			if (stateInfo.getState() != TaskState.REMOVED)
				getTaskListForState(stateInfo.getState()).add(stateInfo);
		}
		
		TaskStateUpdate update = new TaskStateUpdate(systemId, taskId, stateInfo.getState(), stateInfo.layer.getId(), stateInfo.progress);
		serverStateInfo.taskStateChanges.update(new MappedSharedDataUpdate<TaskStateUpdate>(systemId.toString()+taskId, update));
		return update;
	}

	public void taskStateUpdated(TaskStateUpdate updateMessage) {
		serverStateInfo.taskStateChanges.update(new MappedSharedDataUpdate<TaskStateUpdate>(systemId.toString()+updateMessage.getTaskId(), updateMessage));
	}
}
