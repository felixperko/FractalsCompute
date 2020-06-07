package de.felixperko.fractals.system.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import com.aparapi.Kernel;
import com.aparapi.Kernel.Local;
import com.aparapi.device.Device;
import com.aparapi.device.Device.TYPE;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public abstract class GPUExpressionKernel extends Kernel{

	static int TEST_COUNTER = 0;
	static List<Device> suitable_devices;
	
	final Device device;
	
	final int[] pixelItArr;
	final float[] resultsArr;
	final int[] pixelSamples;
	final int[] pixelWasSampled;
	
	final double[] params;
	final String[] paramNames;

	final float[] sampleOffsets;
	
	final String[] constantNames;
	
	final int[] optionsInt = new int[4]; //maxIterations, chunkSize, sampleCount
	final float[] optionsFloat = new float[1]; //bailout
	
	int[] instructions; //0: instructionsLength; 1: dataBufferSize; [instr1, instr2,...]
//	int[] bufferIdx; // [{r1, i1, r2, i2},...]
	
	@Local final float[] data;
	@Constant final double[] constants;
	
//	public static GPUExpressionKernel createKernel(GPUExpression expression, List<ParamSupplier> paramSuppliers, List<Layer> layers, int pixelCount){
//		
//		int id = TEST_COUNTER++;
//		
//		KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
//		if (suitable_devices == null){
//			suitable_devices = new ArrayList<>(preferences.getPreferredDevices(null));
//			Iterator<Device> it = suitable_devices.iterator();
//			while (it.hasNext()){
//				if (it.next().getType() != TYPE.GPU)
//					it.remove();
//			}
//		}
//		int device = id % suitable_devices.size();
//		
//		List<Device> deviceList = new ArrayList<>(suitable_devices.subList(device, device+1)); 
//		KernelManager.instance().setDefaultPreferredDevices(new LinkedHashSet<>(deviceList));
//		
//		
//		ParamSupplier[] filteredParams = new ParamSupplier[expression.variables.size()+expression.constants.size()+1];
//		for (ParamSupplier supp : paramSuppliers)
//			if (supp.getName().equalsIgnoreCase("start"))
//				filteredParams[0] = supp;
//		int i = 0;
//		for (java.util.Map.Entry<ParamSupplier, Integer> e : expression.constants.entrySet())
//			filteredParams[e.getValue()/2] = e.getKey();
//		for (java.util.Map.Entry<ParamSupplier, Integer> e : expression.variables.entrySet())
//			filteredParams[e.getValue()/2] = e.getKey();
//		
//		List<String> constantNames = new ArrayList<>();
//		for (ParamSupplier supp : expression.constants.keySet()){
//			constantNames.add(supp.getName());
//		}
//		
//		GPUExpressionKernel kernel = kernelFactory.createKernel(deviceList.get(0), expression, Arrays.asList(filteredParams), constantNames, layers, pixelCount);
//		KernelManager.instance().setPreferredDevices(kernel, new LinkedHashSet<>(deviceList));
//		return kernel;
//	}
	
	public static GPUExpressionKernel createKernel(IGpuExpressionKernelFactory kernelFactory, GPUExpression expression,
			List<Layer> layers, int pixelCount){
		GPUExpressionKernel kernel;
		
		synchronized (GPUExpressionKernel.class) {
			int id = TEST_COUNTER++;
			
			KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
			if (suitable_devices == null){
				suitable_devices = new ArrayList<>(preferences.getPreferredDevices(null));
				Iterator<Device> it = suitable_devices.iterator();
				while (it.hasNext()){
					if (it.next().getType() != TYPE.GPU)
						it.remove();
				}
			}
			int device = id % suitable_devices.size();
			
			List<Device> deviceList = new ArrayList<>(suitable_devices.subList(device, device+1)); 
			KernelManager.instance().setDefaultPreferredDevices(new LinkedHashSet<>(deviceList));
			
			
//			ParamSupplier[] filteredParams = new ParamSupplier[expression.variables.size()+expression.constants.size()+1];
//			for (ParamSupplier supp : expression.getParameterList())
//				if (supp.getName().equalsIgnoreCase("start"))
//					filteredParams[0] = supp;
//			int i = 0;
//			for (java.util.Map.Entry<ParamSupplier, Integer> e : expression.constants.entrySet())
//				filteredParams[e.getValue()/2] = e.getKey();
//			for (java.util.Map.Entry<ParamSupplier, Integer> e : expression.variables.entrySet())
//				filteredParams[e.getValue()/2] = e.getKey();
			
//			List<String> constantNames = new ArrayList<>();
//			for (ParamSupplier supp : expression.constants.keySet()){
//				constantNames.add(supp.getName());
//			}
			
			kernel = kernelFactory.createKernel(
					deviceList.get(0), expression, expression.getParameterList(), expression.getConstantNames(), layers, pixelCount);
			KernelManager.instance().setPreferredDevices(kernel, new LinkedHashSet<>(deviceList));
			
		}
		return kernel;
	}

	static final int LOCAL_SIZE = 256;
	
	static final int IDX_Z_R = 0;
	static final int IDX_Z_I = 1;
	static final int IDX_SMOOTHSTEP_POW_R = 2;
	static final int IDX_SMOOTHSTEP_POW_I = 3;
	
	//
	//CPU Code
	//
	
	String expression;
	
 	protected GPUExpressionKernel(Device device, GPUExpression expression, List<ParamSupplier> paramSuppliers, List<String> constantNames, List<Layer> layers, int pixelCount){
		
		this.device = device;
		this.expression = expression.getText();
		
		pixelItArr = new int[pixelCount];
		resultsArr = new float[pixelCount];
		pixelSamples = new int[pixelCount];
		pixelWasSampled = new int[pixelCount];
		
		ParamContainer paramContainer = new ParamContainer(paramSuppliers);
		
		params = new double[paramSuppliers.size()*3];
		paramNames = new String[paramSuppliers.size()];
		for (int i = 0 ; i < paramSuppliers.size() ; i++)
			paramNames[i] = paramSuppliers.get(i).getName();
		sampleOffsets = new float[layers.get(layers.size()-1).getSampleCount()*2];
		
		this.constantNames = new String[constantNames.size()];
		this.constants = new double[constantNames.size()*2];
		
		for (int i = 0 ; i < constantNames.size() ; i++){
			String constantName = constantNames.get(i);
			this.constantNames[i] = constantName;
			ComplexNumber value = paramContainer.getClientParameter(constantName).getGeneral(ComplexNumber.class);
			this.constants[i*2  ] = value.realDouble();
			this.constants[i*2+1] = value.imagDouble();
		}
		
		List<GPUInstruction> instructions = expression.instructions;
		
		int dataBufferSize = expression.getRequiredVariableSlots();
		int instructionsBufferSize = 2+instructions.size();
		this.instructions = new int[instructionsBufferSize];
		this.instructions[0] = instructionsBufferSize;
		this.instructions[1] = dataBufferSize;
		
//		bufferIdx = new int[instructions.size()*4];
//		
//		for (int i = 0 ; i < instructions.size() ; i++){
//			GPUInstruction expr = instructions.get(i);
//			this.instructions[i+2] = expr.type;
//			bufferIdx[i*4+0] = expr.fromReal;
//			bufferIdx[i*4+1] = expr.fromImag;
//			bufferIdx[i*4+2] = expr.toReal;
//			bufferIdx[i*4+3] = expr.toImag;
//		}
		data = new float[LOCAL_SIZE*dataBufferSize];
	}
	
	public boolean hasExpressionChanged(String currentExpression){
		return !expression.equalsIgnoreCase(currentExpression);
	}
	
	public Device getDevice(){
		return device;
	}
	
	//
	//GPU Code
	//
	
	public void run() {
		
		boolean stop = false;
		boolean finished = false;
		
		int i = getGlobalId();
		
		float pixelIt = pixelItArr[i];
		
//		if (pixelIt >= 0 && optionsInt[2] - pixelSamples[i] > 0){
		if (optionsInt[2] - pixelSamples[i] > 0){

			pixelSamples[i] = pixelSamples[i] + 1;
			pixelWasSampled[i] = 1;
			
			int index = i;
			float x = (index / optionsInt[1]) + sampleOffsets[pixelSamples[i]*2];
			float y = (index % optionsInt[1]) + sampleOffsets[pixelSamples[i]*2+1];
			
//			boolean doublePrecision = false;
			
			int dataOffset = getLocalId()*instructions[1];
			for (int j = 0 ; j < params.length/3 ; j++){
				data[j*2+0+dataOffset] = (float)(params[j*3+0] + x * params[j*3+2]);
				data[j*2+1+dataOffset] = (float)(params[j*3+1] + y * params[j*3+2]);
			}
			for (int j = params.length/3 ; j < instructions[1]/2 ; j++){
				data[j*2+0+dataOffset] = 0;
				data[j*2+1+dataOffset] = 0;
			}
			
			
			int r1, i1, r2, i2;
			float temp1, temp2, temp3;
			
			float limit = optionsFloat[0]*optionsFloat[0];
			
			while (!stop){
				
				iterate(dataOffset);
//					
				//loop handling
				pixelIt++;
				finished = data[IDX_Z_R + dataOffset]*data[IDX_Z_R + dataOffset]+data[IDX_Z_I + dataOffset]*data[IDX_Z_I + dataOffset] > limit;
				if (finished)
					pixelIt = (float) (pixelIt + 1 - log(log(data[IDX_Z_R + dataOffset]*data[IDX_Z_R + dataOffset]+data[IDX_Z_I + dataOffset]*data[IDX_Z_I + dataOffset])*0.5/log(2))
					/log(sqrt(data[IDX_SMOOTHSTEP_POW_R + dataOffset]*data[IDX_SMOOTHSTEP_POW_R + dataOffset]+data[IDX_SMOOTHSTEP_POW_I + dataOffset]*data[IDX_SMOOTHSTEP_POW_I + dataOffset]))); //TODO why 2*?
//					
				stop = finished || pixelIt >= optionsInt[0];
			}
		}
		if (finished){
			resultsArr[i] = pixelIt;
		}
		else {
			resultsArr[i] = -1;
		}
	}

	protected abstract void iterate(int dataOffset);
//	protected void iterate(int dataOffset) {
//		int r1;
//		int i1;
//		int r2;
//		int i2;
//		float temp1;
//		float temp2;
//		float temp3;
//		for (int idx = 2 ; idx < instructions[0] ; idx++){
//			
//			final int expr = instructions[idx];
//			r1 = bufferIdx[idx*4-8] + dataOffset;
//			i1 = bufferIdx[idx*4-7] + dataOffset;
//			r2 = bufferIdx[idx*4-6] + dataOffset;
//			i2 = bufferIdx[idx*4-5] + dataOffset;
//			
//			if (expr == GPUInstruction.INSTR_POW){
//			}
//			else if (expr == GPUInstruction.INSTR_SQUARE){
//				instr_square(r1, i1);
//			}
//			else if (expr == GPUInstruction.INSTR_MULT){
//				instr_mult(r1, i1, r2, i2);
//			}
//			else if (expr == GPUInstruction.INSTR_ADD){
//				instr_add(r1, i1, r2, i2);
//				
//			}
//			else if (expr == GPUInstruction.INSTR_WRITE_COMPLEX){
//				instr_writecomplex(r1, i1, r2, i2);
//				
//			}
////						else if (expr == EXPR_DIV){
////							//TODO
////							
////						}
////						else
//			else if (expr == GPUInstruction.INSTR_ABS){
//				instr_abs(r1, i1);
//			}
//			
//			else if (expr == GPUInstruction.INSTR_SIN){
//				instr_sin(r1, i1);
//			}
//			else if (expr == GPUInstruction.INSTR_COS){
//				instr_cos(r1, i1);
//			}
//		}
//	}

	protected void instr_sin(int r1, int i1) {
		float temp1;
		temp1 = sin(data[r1]) * cosh(data[i1]);
		data[i1] = cos(data[r1]) * sinh(data[i1]);
		data[r1] = temp1;
	}

	protected void instr_cos(int r1, int i1) {
		float temp1;
		temp1 = cos(data[r1]) * cosh(data[i1]);
		data[i1] = -sin(data[r1]) * sinh(data[i1]);
		data[r1] = temp1;
	}
	
	protected void instr_tan(int r1, int i1) {
		//tan(a + bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i
		//https://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/complex/Complex.html#tan()
		float temp1;
		temp1 = sin(2*data[r1])/(cos(2*data[r1])+cosh(2*data[i1]));
		data[i1] = sinh(2*data[i1])/(cos(2*data[r1])+cosh(2*data[i1]));
		data[r1] = temp1;
	}

	protected void instr_sinh(int r1, int i1) {
		//sinh(a + bi) = sinh(a)cos(b)) + cosh(a)sin(b)i
		float temp1;
		temp1 = sinh(data[r1]) * cos(data[i1]);
		data[i1] = cosh(data[r1]) * sin(data[i1]);
		data[r1] = temp1;
	}

	protected void instr_cosh(int r1, int i1) {
		//cosh(a + bi) = cosh(a)cos(b) + sinh(a)sin(b)i
		float temp1;
		temp1 = cosh(data[r1]) * cos(data[i1]);
		data[i1] = sinh(data[r1]) * sin(data[i1]);
		data[r1] = temp1;
	}
	
	protected void instr_tanh(int r1, int i1) {
		//tanh(a + bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i
		float temp1; 
		temp1 = sinh(2*data[r1])/(cosh(2*data[r1])+cos(2*data[i1]));
		data[i1] = sin(2*data[i1])/(cosh(2*data[r1])+cos(2*data[i1]));
		data[r1] = temp1;
	}

	protected void instr_abs(int r1, int i1) {
		data[r1] = abs(data[r1]);
		data[i1] = abs(data[i1]);
	}

	protected void instr_conj(int r1, int i1) {
		data[i1] = -data[i1];
	}

	protected void instr_writecomplex(int r1, int i1, int r2, int i2) {
		data[r2] = data[r1];
		data[i2] = data[i1];
	}

	protected void instr_add(int r1, int i1, int r2, int i2) {
		data[r1] = data[r1]+data[r2];
		data[i1] = data[i1]+data[i2];
	}

	protected void instr_sub(int r1, int i1, int r2, int i2) {
		data[r1] = data[r1]-data[r2];
		data[i1] = data[i1]-data[i2];
	}

	protected void instr_square(int r1, int i1) {
		float temp1;
		temp1 = data[r1]*data[r1] - data[i1]*data[i1];
		data[i1] = data[r1]*data[i1]*2;
		data[r1] = temp1;
	}

	protected void instr_mult(int r1, int i1, int r2, int i2) {
		float temp1;
		temp1 = data[r1]*data[r2] - data[i1]*data[i2];
		data[i1] = (data[r1]+data[i1])*(data[r2]+data[i2]) - temp1;
		data[r1] = temp1;
	}
	
	protected void instr_pow(int r1, int i1, int r2, int i2){
		float temp1;
		float temp2;
		float temp3;
		temp3 = sqrt(data[r1]*data[r1]+data[i1]*data[i1]);
		if (temp3 != 0){
			temp3 = log(temp3);
			temp1 = atan2(data[i1], data[r1]);
			//mult
			temp2 = (temp3*data[r2] - temp1*data[i2]);
			temp1 = (temp3*data[i2] + temp1*data[r2]);
			temp3 = temp2;
			//exp
			temp2 = exp(temp3);
			data[r1] = temp2 * cos(temp1);
			data[i1] = temp2 * sin(temp1);
		} else {
			data[r1] = data[r1]; //Workaround for "child list broken" in nested if-clause without else
		}
	}
}
