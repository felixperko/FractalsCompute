package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemStateInfo implements Serializable{
	
	private static final long serialVersionUID = -2682394717324577351L;
	
//	int stageCount;
//	int currentWorkerThreads;
	Map<Integer, TaskStateInfo> taskStates = new ConcurrentHashMap<>();
	
	Map<TaskState, List<TaskStateInfo>> tasksPerState = new ConcurrentHashMap<>();
	
	ServerStateInfo serverStateInfo;
	
	public void addTaskStateInfo(TaskStateInfo taskStateInfo) {
		taskStateInfo.setSystemStateInfo(this);
		taskStates.put((Integer)taskStateInfo.getTaskId(), taskStateInfo);
		
		getTaskListForState(taskStateInfo.getState()).add(taskStateInfo);
		updateTime();
	}
	
	public List<TaskStateInfo> getTaskListForState(TaskState state){
		List<TaskStateInfo> stateList = tasksPerState.get(state);
		if (stateList == null) {
			stateList = new CopyOnWriteArrayList<>();
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
	
	protected void updateTime(){
		serverStateInfo.updateTime();
	}
	
	public long getUpdateTime(){
		return serverStateInfo.getUpdateTime();
	}
}
