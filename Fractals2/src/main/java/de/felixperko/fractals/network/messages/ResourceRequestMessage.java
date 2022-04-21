package de.felixperko.fractals.network.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.manager.server.ResourceManager;
import de.felixperko.fractals.network.infra.ClientMessage;

public class ResourceRequestMessage extends ClientMessage{

	private static final long serialVersionUID = -1844785845754276311L;
	
	private int cpuCores;
	private List<Float> requestedGpuUsages = new ArrayList<>();
	private Map<String, Float> existingGpuUsages = new HashMap<>();

	public ResourceRequestMessage(int cpuCores, List<Float> requestedGpuUsages, Map<String, Float> existingGpuUsages) {
		this.cpuCores = cpuCores;
		this.requestedGpuUsages = requestedGpuUsages;
		this.existingGpuUsages = existingGpuUsages;
	}

	@Override
	protected void process() {
		ResourceManager resourceManager = getReceiverManagers().getResourceManager();
		resourceManager.resourcesRequested(cpuCores, requestedGpuUsages, existingGpuUsages);
		answer(new ResourceResponseMessage(resourceManager.getCpuCores(), resourceManager.getMaxCpuCores(), resourceManager.getGpuResources()));
	}

}
