package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemStateInfo implements Serializable{
	
	private static final long serialVersionUID = -2682394717324577351L;
	
//	int stageCount;
//	int currentWorkerThreads;
	Map<Integer, TaskStateInfo> taskStates = new HashMap<>();
	
	Map<TaskState, List<TaskStateInfo>> tasksPerState = new HashMap<>();
	
	ServerStateInfo serverStateInfo;
	
	public void addTaskStateInfo(TaskStateInfo taskStateInfo) {
		taskStateInfo.setSystemStateInfo(this);
		taskStates.put((Integer)taskStateInfo.getTaskId(), taskStateInfo);
		
		getTaskListForState(taskStateInfo.getState()).add(taskStateInfo);
	}
	
	public List<TaskStateInfo> getTaskListForState(TaskState state){
		List<TaskStateInfo> stateList = tasksPerState.get(state);
		if (stateList == null) {
			stateList = new ArrayList<>();
			tasksPerState.put(state, stateList);
		}
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
	
	public Map<TaskState, List<TaskStateInfo>> getTasksPerState(){
		return tasksPerState;
	}
}
