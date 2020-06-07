package de.felixperko.fractals.system.calculator;

import java.util.List;

import com.aparapi.device.Device;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public interface IGpuExpressionKernelFactory {
	
	public GPUExpressionKernel createKernel(Device device, GPUExpression expression, List<ParamSupplier> paramSuppliers, List<String> constantNames, List<Layer> layers, int pixelCount);

}
