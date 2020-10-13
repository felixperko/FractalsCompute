package de.felixperko.fractals.network.messages;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.network.infra.ServerMessage;

public class ResourceResponseMessage extends ServerMessage{

	private static final long serialVersionUID = -3695039318746877525L;
	
	private int cpuCores, maxCpuCores;
	private Map<String, Float> gpus = new HashMap<>();
	
	public ResourceResponseMessage(int cpuCores, int maxCpuCores, Map<String, Float> gpus) {
		super();
		this.cpuCores = cpuCores;
		this.maxCpuCores = maxCpuCores;
		this.gpus = gpus;
	}

	@Override
	protected void process() {
		getClientMessageInterface().changedResources(cpuCores, maxCpuCores, gpus);
	}
}
