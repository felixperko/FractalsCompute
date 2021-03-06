package de.felixperko.fractals.system.systems.stateinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.network.infra.connection.Connection;
import de.felixperko.fractals.system.numbers.ComplexNumber;

public class ServerStateInfo implements Serializable{
	
	private static final long serialVersionUID = -7691605222394919234L;
	
	int localWorkerThreads;
	List<Integer> remoteWorkerThreads = new ArrayList<>();
	Map<UUID, SystemStateInfo> systemStates = new HashMap<>();
	
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
	
	public List<DataContainer> getSharedDataUpdates(Connection connection){
		List<DataContainer> list = new ArrayList<>();
		//TODO subscribe system
		for (SystemStateInfo ssi : systemStates.values())
			list.addAll(ssi.getSharedDataUpdates(connection));
		return list;
	}

	public void updateMidpoint(UUID systemId, ComplexNumber midpoint) {
		systemStates.get(systemId).updateMidpoint(midpoint);
	}
}
