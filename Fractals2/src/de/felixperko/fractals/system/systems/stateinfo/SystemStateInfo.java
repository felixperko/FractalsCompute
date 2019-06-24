package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemStateInfo implements Serializable{
	
	private static final long serialVersionUID = -2682394717324577351L;
	
//	int stageCount;
//	int currentWorkerThreads;
	Map<Integer, TaskStateInfo> taskStates = new ConcurrentHashMap<>();
	
	ServerStateInfo serverStateInfo;
	
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

	
	public void taskStateChanged(int taskId, TaskState oldState, TaskStateInfo stateInfo) {
		getTaskListForState(oldState).remove(stateInfo);
		if (stateInfo.getState() != TaskState.REMOVED)
			getTaskListForState(stateInfo.getState()).add(stateInfo);
	}
}
