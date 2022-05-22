package de.felixperko.fractals.system.calculator.EscapeTime;

import java.util.List;
import java.util.Map;

import com.aparapi.Kernel;
import com.aparapi.device.Device;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public abstract class EscapeTimeGpuKernelAbstract extends Kernel {

	static int ID_COUNTER = 0;
	protected ComputeKernelParameters kernelParameters;
	protected final Device device;
	protected final int[] pixelItArr;
	protected final float[] resultsArr;
	protected final int[] pixelSamples;
	protected final int[] pixelWasSampled;
	protected final double[] params;
	protected final String[] paramNames;
	protected final float[] sampleOffsets;
	protected final int[] optionsInt = new int[4];
	protected final float[] optionsFloat = new float[2];
	protected Map<String, ParamSupplier> constantSuppliers;
	protected int[] instructions;
	@Constant
	protected final double[] constants;
	protected static final int IDX_Z_R = 0;
	protected static final int IDX_Z_I = 1;
	protected static final int IDX_SMOOTHSTEP_POW_R = 4;
	protected static final int IDX_SMOOTHSTEP_POW_I = 5;
	protected String expression;

	public EscapeTimeGpuKernelAbstract(Device device, ComputeKernelParameters kernelParameters) {
		this.device = device;
		
		this.kernelParameters = kernelParameters;
		this.expression = kernelParameters.getMainExpression().getText();
		
		List<ParamSupplier> paramSuppliers = kernelParameters.getMainExpression().getParameterList();
		constantSuppliers = kernelParameters.getMainExpression().getConstants();
		
		int pixelCount = kernelParameters.getPixelCount();
		pixelItArr = new int[pixelCount];
		resultsArr = new float[pixelCount];
		pixelSamples = new int[pixelCount];
		pixelWasSampled = new int[pixelCount];
		
		params = new double[paramSuppliers.size()*3];
		paramNames = new String[paramSuppliers.size()];
		for (int i = 0 ; i < paramSuppliers.size() ; i++)
			paramNames[i] = paramSuppliers.get(i).getUID();
		List<Layer> layers = kernelParameters.getLayers();
		sampleOffsets = new float[layers.get(layers.size()-1).getSampleCount()*2];
		
		this.optionsFloat[1] = (float)kernelParameters.getMainExpression().getSmoothstepConstant();
		
//		this.constantNames = new String[constantNames.size()];
		this.constants = new double[constantSuppliers.size()*2];
	}

	public ComputeKernelParameters getKernelParameters() {
		return kernelParameters;
	}

	public boolean hasExpressionChanged(String currentExpression) {
		return !expression.equalsIgnoreCase(currentExpression);
	}

	public Device getDevice() {
		return device;
	}

}