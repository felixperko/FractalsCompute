package de.felixperko.fractals.manager.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import com.aparapi.internal.opencl.OpenCLPlatform;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeGpuKernelAbstract;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeKernelFactoryAsm;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeGpuKernel32;

public class GPUKernelManager {
	
	List<Device> devices;
	
	Map<String, ComputeExpression> expressions = new HashMap<>();
	
	List<ComputeKernelParameters> keyParametersInsertionOrdered = new ArrayList<>();
	LinkedList<ComputeKernelParameters> keyParametersUsageOrdered = new LinkedList<>();
	
	Map<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>> generalKernelsAvailable = new HashMap<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>>();
	Map<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>> generalKernelsInUse = new HashMap<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>>();
	Map<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>> optimizedKernelsAvailable = new HashMap<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>>();
	Map<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>> optimizedKernelsInUse = new HashMap<ComputeKernelParameters, LinkedList<EscapeTimeGpuKernelAbstract>>();
	
	int kernelsPerDevice = 2;
	
	EscapeTimeKernelFactoryAsm kernelFactory = new EscapeTimeKernelFactoryAsm();
	
	public GPUKernelManager(List<Device> devices){
		this.devices = devices;
	}
	
	public synchronized EscapeTimeGpuKernelAbstract getKernel(ComputeKernelParameters kernelParams, ParamContainer fractalParams){
		
		ComputeKernelParameters checkKernelParams = getCheckKernelParams(kernelParams, fractalParams);
		LinkedList<EscapeTimeGpuKernelAbstract> listForParameters = null;
		
		if (checkKernelParams == null){
			//no applicable list yet
			checkKernelParams = kernelParams;
			keyParametersInsertionOrdered.add(kernelParams);
			keyParametersUsageOrdered.add(kernelParams);
			listForParameters = new LinkedList<EscapeTimeGpuKernelAbstract>();
			generalKernelsAvailable.put(checkKernelParams, listForParameters);
		}
		else {
			listForParameters = generalKernelsAvailable.get(checkKernelParams);
			if (listForParameters == null){
				listForParameters = new LinkedList<>();
				generalKernelsAvailable.put(checkKernelParams, listForParameters);
			}
		}
		
		EscapeTimeGpuKernelAbstract kernel = listForParameters.poll();
		if (kernel == null)
			kernel = createKernel(checkKernelParams);
		kernelInUse(checkKernelParams, kernel);
		
		return kernel;
	}

	protected ComputeKernelParameters getCheckKernelParams(ComputeKernelParameters kernelParams, ParamContainer fractalParams) {
		ComputeKernelParameters checkKernelParams = null;
		
		//check if used in the past
		ComputeKernelParameters last = keyParametersInsertionOrdered.isEmpty() ? null : keyParametersUsageOrdered.getLast();
		boolean wasLastUsed = last != null && last.isCompartible(kernelParams, fractalParams);
		if (wasLastUsed){
			checkKernelParams = last;
		}
		else {
			//check insertion history
			for (int i = keyParametersInsertionOrdered.size()-1 ; i >= 0 ; i--){
				ComputeKernelParameters keyParameter = keyParametersInsertionOrdered.get(i);
				if (keyParameter.isCompartible(kernelParams, fractalParams)){
					checkKernelParams = keyParameter;
					keyParametersUsageOrdered.removeLastOccurrence(checkKernelParams);
					keyParametersUsageOrdered.add(checkKernelParams);
					break;
				}
			}
		}
		return checkKernelParams;
	}

	private EscapeTimeGpuKernelAbstract createKernel(ComputeKernelParameters kernelParameters) {
		//count device usages
		Map<Device, Integer> existingKernelCounts = new HashMap<>();
		generalKernelsInUse.forEach((k, list) -> {
			list.forEach(kernel -> countDeviceKernels(existingKernelCounts, kernel));
		});
		generalKernelsAvailable.forEach((k, list) -> {
			list.forEach(kernel -> countDeviceKernels(existingKernelCounts, kernel));
		});
		
		//check if devices are unused
		for (Device device : devices){
			if (!existingKernelCounts.containsKey(device)){
				return createKernelOnDevice(device, kernelParameters);
			}
		}
		
		//choose device with lowest usage
		Device device = null;
		int lowestCount = Integer.MAX_VALUE;
		for (Entry<Device, Integer> e : existingKernelCounts.entrySet()){
			if (e.getValue() < lowestCount){
				device = e.getKey();
				lowestCount = e.getValue();
			}
		}
		
		return createKernelOnDevice(device, kernelParameters);
	}

	private EscapeTimeGpuKernelAbstract createKernelOnDevice(Device device, ComputeKernelParameters kernelParameters) {
		System.out.println("create kernel on device: "+device.getDeviceId());
		LinkedHashSet<Device> set = new LinkedHashSet<>();
		set.add(device);
		KernelManager.instance().setDefaultPreferredDevices(set);

		EscapeTimeGpuKernelAbstract kernel = kernelFactory.createKernel(device, kernelParameters);
		LinkedHashSet<Device> set2 = new LinkedHashSet<>();
		set2.add(device);
		KernelManager.instance().setPreferredDevices(kernel, new LinkedHashSet<>(set2));
		return kernel;
	}

	private void countDeviceKernels(Map<Device, Integer> existingKernelCount, EscapeTimeGpuKernelAbstract kernel) {
		Integer count = existingKernelCount.get(kernel.getDevice());
		if (count == null)
			existingKernelCount.put(kernel.getDevice(), 1);
		else
			existingKernelCount.put(kernel.getDevice(), ++count);
	}

	private void kernelInUse(ComputeKernelParameters kernelParams, EscapeTimeGpuKernelAbstract kernel) {
		LinkedList<EscapeTimeGpuKernelAbstract> list = generalKernelsInUse.get(kernelParams);
		if (list == null){
			list = new LinkedList<>();
			generalKernelsInUse.put(kernelParams, list);
		}
		list.add(kernel);
	}

	public synchronized void kernelNoLongerInUse(EscapeTimeGpuKernelAbstract kernel, ParamContainer usedServerParams) {
		ComputeKernelParameters checkParams = getCheckKernelParams(kernel.getKernelParameters(), usedServerParams); 
		LinkedList<EscapeTimeGpuKernelAbstract> list_old = generalKernelsInUse.get(checkParams);
		list_old.remove(kernel);
		LinkedList<EscapeTimeGpuKernelAbstract> list = generalKernelsAvailable.get(checkParams);
		if (list == null){
			list = new LinkedList<>();
			generalKernelsAvailable.put(checkParams, list);
		}
		list.add(kernel);
	}
}
