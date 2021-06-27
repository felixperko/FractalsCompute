package de.felixperko.fractals.manager.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.device.Device;
import com.aparapi.device.Device.TYPE;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import com.aparapi.internal.opencl.OpenCLPlatform;

import de.felixperko.fractals.manager.common.Manager;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.system.calculator.infra.DeviceType;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstTaskManager;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.util.NumberUtil;

public class ResourceManager extends Manager{
	
	private static final Logger LOG = LoggerFactory.getLogger(BreadthFirstTaskManager.class);
	
	private GPUKernelManager gpuKernelManager;
	
	int maxCores = Runtime.getRuntime().availableProcessors();
	
	int cpuCores;
	
	Map<Device, Float> gpus = new HashMap<>();

	Map<Device, String> namesOfGpus = new HashMap<>();
	Map<String, Device> gpusForName = new HashMap<>();

	Map<Device, Integer> gpusActiveKernels = new HashMap<>();
	Map<Device, Long> gpusActiveKernelStartTimes = new HashMap<>();
	Map<Device, Long> gpusCooldownStartTimes = new HashMap<>();
	Map<Device, Long> gpusCooldowns = new HashMap<>();
	
	public ResourceManager(Managers managers) {
		super(managers);
	      List<OpenCLPlatform> platforms = (new OpenCLPlatform()).getOpenCLPlatforms();
	      System.out.println("Machine contains " + platforms.size() + " OpenCL platforms");
	      int platformc = 0;
	      int totalGpuCounter = 1;
	      for (OpenCLPlatform platform : platforms) {
	         System.out.println("Platform " + platformc + "{");
	         System.out.println("   Name    : \"" + platform.getName() + "\"");
	         System.out.println("   Vendor  : \"" + platform.getVendor() + "\"");
	         System.out.println("   Version : \"" + platform.getVersion() + "\"");
	         List<OpenCLDevice> devices = platform.getOpenCLDevices();
	         System.out.println("   Platform contains " + devices.size() + " OpenCL devices");
	         int devicec = 1;
	         for (OpenCLDevice device : devices) {
	            System.out.println("   Device " + devicec + "{");
	            System.out.println("       Type                  : " + device.getType());
	            System.out.println("       GlobalMemSize         : " + device.getGlobalMemSize());
	            System.out.println("       LocalMemSize          : " + device.getLocalMemSize());
	            System.out.println("       MaxComputeUnits       : " + device.getMaxComputeUnits());
	            System.out.println("       MaxWorkGroupSizes     : " + device.getMaxWorkGroupSize());
	            System.out.println("       MaxWorkItemDimensions : " + device.getMaxWorkItemDimensions());
	            System.out.println("   }");
	            if (device.getType() == TYPE.GPU) {
			    	gpus.put(device, 0F);
			    	String name = "GPU" + totalGpuCounter + " ("+device.getName()+")";
			    	namesOfGpus.put(device, name);
			    	gpusForName.put(name, device);
		            totalGpuCounter++;
	            }
	            devicec++;
	         }
	         System.out.println("}");
	         platformc++;
	      }

	      KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
	      System.out.println("\nDevices in preferred order:\n");

	      for (Device device : preferences.getPreferredDevices(null)) {
	    	 
	         System.out.println(device);
	         System.out.println();
	      }
	      
	      gpuKernelManager = new GPUKernelManager(new ArrayList<>(gpus.keySet()));
	}
	
	public Map<String, Float> getGpuResources(){
		Map<String, Float> map = new HashMap<>();
		for (Entry<String, Device> e : gpusForName.entrySet()){
			map.put(e.getKey(), gpus.get(e.getValue()));
		}
		return map;
	}

	public GPUKernelManager getGpuKernelManager() {
		return gpuKernelManager;
	}
	
	public void kernelActiveOnDevice(Device device){
		synchronized (this) {
			Integer currentInstances = gpusActiveKernels.get(device);
			if (currentInstances == null){
				gpusActiveKernels.put(device, 1);
			}
			if (currentInstances == null || currentInstances == 0)
				gpusActiveKernelStartTimes.put(device, System.nanoTime());
			else
				gpusActiveKernels.put(device, currentInstances+1);
			
			
		}
	}
	
	public long kernelInactiveOnDevice(Device device){
		synchronized (this) {
			Integer currentInstances = gpusActiveKernels.get(device);
			if (currentInstances == null){
				return 0;
			}
//			if (currentInstances == 1){
				long startTime = gpusActiveKernelStartTimes.get(device);
				long deltaTime = System.nanoTime()-startTime;
				double usageFactor = gpus.get(device);
				double yieldFactor = 1-usageFactor;
				double waitTimeFactor = yieldFactor/usageFactor;
				long yieldTime = yieldFactor == 0 ? 0 : (long) (deltaTime * waitTimeFactor); //TODO yield time if not last active instance?
				
				Long endTime = yieldTime+System.nanoTime();
				Long lastEndTime = getPredictedCooldownEnd(device);
				if (lastEndTime == null || endTime > lastEndTime){
					gpusCooldownStartTimes.put(device, System.nanoTime());
					gpusCooldowns.put(device, deltaTime);
				}
				else if (lastEndTime != null && lastEndTime > endTime)
					yieldTime = lastEndTime-System.nanoTime();
				
				if (currentInstances == 1)
					gpusActiveKernels.remove(device);
				else
					gpusActiveKernels.put(device, currentInstances-1);
				return getPredictedCooldownEnd(device);
//			}
//			return 
		}
	}

	public Long getPredictedCooldownEnd(Device device) {
		double usageFactor = gpus.get(device);
		double yieldFactor = 1-usageFactor;
		if (yieldFactor == 0)
			return System.nanoTime();
		if (usageFactor == 0)
			return System.nanoTime();
		double waitTimeFactor = yieldFactor/usageFactor;
		Long cooldownStart = gpusCooldownStartTimes.get(device);
		Long cooldownDuration = gpusCooldowns.get(device);
		if (cooldownStart == null || cooldownDuration == null)
			return null;
		return (long) (cooldownStart+cooldownDuration*waitTimeFactor);
	}

	public synchronized boolean resourcesRequested(int cpuCores, List<Float> requestedGpuUsages, Map<String, Float> existingGpuUsages) {
		
		Map<Device, Float> newGpuResources = new HashMap<>();
		if (existingGpuUsages != null){
			for (Entry<String, Float> e : existingGpuUsages.entrySet()){
				Device device = gpusForName.get(e.getKey());
				if (device == null){
					LOG.error("Requested resources for specific, but unknown device: "+e.getKey());
					continue;
				}
				if (e.getValue() < 0 || e.getValue() > 1){
					LOG.error("Requested resources with invalid max usage of ("+e.getValue()+") for Device: "+e.getKey()+". Values must be between 0f and 1f.");
					continue;
				}
				newGpuResources.put(device, e.getValue());
			}
		}
		
		int i = 0;
		Device device = null;
		List<Device> devices = new ArrayList<>(gpus.keySet());
		//TODO check if requested gpu usages are satisfied already, keep existing devices
		if (requestedGpuUsages != null){
			requestNewGpuLoop:
			for (float requestedGpuUsage : requestedGpuUsages){
				while (device == null || newGpuResources.containsKey(device)){
					if (i >= devices.size())
						break requestNewGpuLoop;
					device = devices.get(i);
					i++;
				}
				newGpuResources.put(device, requestedGpuUsage);
				device = null;
			}
		}
		
		//keep existing gpus
		for (Entry<Device, Float> e : this.gpus.entrySet()){
			if (!newGpuResources.containsKey(e.getKey()))
				newGpuResources.put(e.getKey(), e.getValue());
		}
		
		if (cpuCores < 0 || cpuCores > maxCores){
			LOG.error("Requested invalid number of cpu cores ("+cpuCores+"). Values must be between 0 and "+maxCores+".");
			cpuCores = Math.max(0, Math.min(maxCores, cpuCores));
		}
		
		return changeResources(cpuCores, newGpuResources);
		
	}
	
	protected boolean changeResources(int cpuCores, Map<Device, Float> gpus) {
		
		//determine changes
		
		int closeCpuThreads = Math.max(0, this.cpuCores-cpuCores);
		int startCpuThreads = Math.max(0, cpuCores-this.cpuCores);
		
		List<Device> deactivateGpus = new ArrayList<>();
		List<Device> activateGpus = new ArrayList<>();
		
		for (Entry<Device, Float> e : gpus.entrySet()){
			Float curr = this.gpus.get(e.getKey());
			if (curr == null || (curr == 0 && e.getValue() > 0))
				activateGpus.add(e.getKey());
		}

		for (Entry<Device, Float> e : this.gpus.entrySet()){
			Float newUsage = gpus.get(e.getKey());
			if (newUsage == null || (e.getValue() > 0 && newUsage == 0))
				deactivateGpus.add(e.getKey());
		}
		
		//make changes
		
		this.gpus = gpus;
		this.cpuCores = cpuCores;
		
		ServerThreadManager threadManager = ((ServerManagers)managers).getThreadManager();
		List<CalculateFractalsThread> calculateThreads = new ArrayList<>(threadManager.getCalculateThreads());
		
		int closeGpuThreads = (deactivateGpus.size()-activateGpus.size())*gpuKernelManager.kernelsPerDevice;
		for (CalculateFractalsThread thread : calculateThreads){
			if (closeCpuThreads > 0 && thread.getDeviceType() == DeviceType.CPU){
				thread.setLifeCycleState(LifeCycleState.STOPPED);
				threadManager.removeThread(thread);
				closeCpuThreads--;
			}
			else if (closeGpuThreads > 0 && thread.getDeviceType() == DeviceType.GPU){
				thread.setLifeCycleState(LifeCycleState.STOPPED);
				threadManager.removeThread(thread);
				closeGpuThreads--;
			}
			
			if (closeCpuThreads == 0 && closeGpuThreads == 0)
				break;
		}
		
		if (startCpuThreads > 0 || activateGpus.size() > 0){
			threadManager.startWorkerThreads(startCpuThreads, activateGpus.size()*gpuKernelManager.kernelsPerDevice, false);
		}
		
		return (!deactivateGpus.isEmpty() || !activateGpus.isEmpty() || startCpuThreads > 0);
	}

	public int getCpuCores() {
		return cpuCores;
	}

	public int getMaxCpuCores() {
		return maxCores;
	}

	public Float getGpuUsage(Device device) {
		return gpus.get(device);
	}
}
