package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerStateInfo implements Serializable{
	
	private static final long serialVersionUID = -7691605222394919234L;
	
	int localWorkerThreads;
	List<Integer> remoteWorkerThreads = new ArrayList<>();
	transient Map<UUID, SystemStateInfo> systemStates = new HashMap<>();
	
	long updateTime = 0;
	
	public void addSystemStateInfo(UUID systemId, SystemStateInfo systemStateInfo) {
		systemStateInfo.setServerStateInfo(this);
		systemStates.put(systemId, systemStateInfo);
	}
	
	public Collection<SystemStateInfo> getSystemStates(){
		return systemStates.values();
	}
	
	public Collection<TaskStateInfo> getTaskStates(UUID systemId){
		return systemStates.get(systemId).taskStates.values();
	}

	public SystemStateInfo getSystemState(UUID systemId) {
		return systemStates.get(systemId);
	}
	
	protected void updateTime(){
		updateTime = System.nanoTime();
	}

	public long getUpdateTime() {
		return updateTime;
	}
}
