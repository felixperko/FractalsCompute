package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import de.felixperko.fractals.data.shareddata.ContinuousSharedData;
import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.data.shareddata.MappedSharedData;
import de.felixperko.fractals.data.shareddata.MappedSharedDataUpdate;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.system.numbers.ComplexNumber;

public class SystemStateInfo implements Serializable{
	
	private static final long serialVersionUID = -2682394717324577351L;
	
//	int stageCount;
//	int currentWorkerThreads;
	Map<Integer, TaskStateInfo> taskStates = new ConcurrentHashMap<>();
	
	MappedSharedData<TaskStateUpdate> taskStateChanges = new MappedSharedData<TaskStateUpdate>("taskStates", true);
	MappedSharedData<ComplexNumberUpdate> currentMidpointData = new MappedSharedData<ComplexNumberUpdate>("currentMidpoint", false);
	ContinuousSharedData<ComplexNumberListUpdate> currentTraces = new ContinuousSharedData<ComplexNumberListUpdate>("currentTraces", true);
	ContinuousSharedData<IterationsPerSecondUpdate> iterationsPerSecond = new ContinuousSharedData<IterationsPerSecondUpdate>("iterationsPerSecond", true);
	
	ServerStateInfo serverStateInfo;
	
	UUID systemId;
	
	public SystemStateInfo(UUID id) {
		this.systemId = id;
	}

	public void addTaskStateInfo(TaskStateInfo taskStateInfo) {
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
		
		TaskStateUpdate update = new TaskStateUpdate(systemId, taskId, stateInfo.getState(), stateInfo.getLayerId(), stateInfo.progress);
		taskStateChanges.update(new MappedSharedDataUpdate<TaskStateUpdate>(systemId.toString()+taskId, update));
		return update;
	}

	public void taskStateUpdated(TaskStateUpdate updateMessage) {
		taskStateChanges.update(new MappedSharedDataUpdate<TaskStateUpdate>(systemId.toString()+updateMessage.getTaskId(), updateMessage));
	}

	public void updateMidpoint(ComplexNumber midpoint) {
		currentMidpointData.update(new MappedSharedDataUpdate<ComplexNumberUpdate>(systemId.toString(), new ComplexNumberUpdate(systemId, midpoint)));
	}
	
	public void updateTraces(List<ComplexNumber> traces) {
		currentTraces.update(new ComplexNumberListUpdate(traces));
	}
	
	public void updateIterationsPerSecond(int timeslice, long startTime, long endTime, int ips_total, int[] ips_threads) {
		iterationsPerSecond.update(new IterationsPerSecondUpdate(timeslice, startTime, endTime, ips_total, ips_threads));
	}

	public List<DataContainer> getSharedDataUpdates(Connection connection) {
		List<DataContainer> list = new ArrayList<>();
		taskStateChanges.getUpdatesAppendList(connection, list);
		currentMidpointData.getUpdatesAppendList(connection, list);
		currentTraces.getUpdatesAppendList(connection, list);
		iterationsPerSecond.getUpdatesAppendList(connection, list);
		return list;
	}
}
