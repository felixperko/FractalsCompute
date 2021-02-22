package de.felixperko.fractals.system.calculator.EscapeTime;

import java.util.List;
import java.util.Map;

import com.aparapi.Kernel.Constant;
import com.aparapi.device.Device;

import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

@Deprecated
public abstract class EscapeTimeCpuKernel implements ComputeKernel {
	
//	final static int OPTION_INT_MAX_ITERATIONS = 0;
//	final static int OPTION_INT_CHUNK_SIZE = 1;
//	final static int OPTION_INT_SAMPLES = 2;
//	
//	static int ID_COUNTER = 0;
//	
//	protected ComputeKernelParameters kernelParameters;
//	protected final int[] pixelItArr;
//	protected final float[] resultsArr;
//	protected final int[] pixelSamples;
//	protected final int[] pixelWasSampled;
//	protected final double[] params;
//	protected final String[] paramNames;
//	protected final float[] sampleOffsets;
//	protected final int[] optionsInt = new int[4];
//	protected final float[] optionsFloat = new float[1];
//	protected Map<String, ParamSupplier> constantSuppliers;
//	
//	protected final double[] constants;
//	
//	protected static final int IDX_Z_R = 0;
//	protected static final int IDX_Z_I = 1;
//	protected static final int IDX_SMOOTHSTEP_POW_R = 4;
//	protected static final int IDX_SMOOTHSTEP_POW_I = 5;
//	protected String expression;
//	
//	double data[];
//	
//	double smoothstepConstant;
//	
//	public EscapeTimeCpuKernel(ComputeKernelParameters kernelParameters) {
//		
//		this.kernelParameters = kernelParameters;
//		this.expression = kernelParameters.getExpression().getText();
//		
//		List<ParamSupplier> paramSuppliers = kernelParameters.getExpression().getParameterList();
//		constantSuppliers = kernelParameters.getExpression().getConstants();
//		
//		int pixelCount = kernelParameters.getPixelCount();
//		pixelItArr = new int[pixelCount];
//		resultsArr = new float[pixelCount];
//		pixelSamples = new int[pixelCount];
//		pixelWasSampled = new int[pixelCount];
//		
//		params = new double[paramSuppliers.size()*3];
//		paramNames = new String[paramSuppliers.size()];
//		for (int i = 0 ; i < paramSuppliers.size() ; i++)
//			paramNames[i] = paramSuppliers.get(i).getName();
//		List<Layer> layers = kernelParameters.getLayers();
//		sampleOffsets = new float[layers.get(layers.size()-1).getSampleCount()*2];
//		
////		this.constantNames = new String[constantNames.size()];
//		this.constants = new double[constantSuppliers.size()*2];
//		this.data = new double[pixelCount];
//		
//		this.smoothstepConstant = kernelParameters.getExpression().getSmoothstepConstant();
//	}
//	
//	public void run(int pixel, int localBufferIndex) {
//		
////		boolean stop = false;
//		boolean finished = false;
//		
//		int i = pixel;
//		
//		float pixelIt = pixelItArr[i];
//		
////		if (pixelIt >= 0 && optionsInt[2] - pixelSamples[i] > 0){
////		if (optionsInt[OPTION_INT_SAMPLES] - pixelSamples[i] > 0){
//			
//			int index = i;
//			float x = (index / optionsInt[OPTION_INT_CHUNK_SIZE]) + sampleOffsets[pixelSamples[i]*2];
//			float y = (index % optionsInt[OPTION_INT_CHUNK_SIZE]) + sampleOffsets[pixelSamples[i]*2+1];
//			
////			pixelSamples[i] = pixelSamples[i] + 1;
////			pixelWasSampled[i] = 1;
//			
////			boolean doublePrecision = false;
//			
//			int localDataSize = kernelParameters.getExpression().getRequiredVariableSlots();
//			int dataOffset = localBufferIndex*localDataSize;
//			for (int j = 0 ; j < params.length/3 ; j++){
//				data[j*2+0+dataOffset] = (params[j*3+0] + x * params[j*3+2]);
//				data[j*2+1+dataOffset] = (params[j*3+1] + y * params[j*3+2]);
//			}
//			for (int j = params.length/3 ; j < localDataSize/2 ; j++){
//				data[j*2+0+dataOffset] = 0;
//				data[j*2+1+dataOffset] = 0;
//			}
//			
//			
//			int r1, i1, r2, i2;
//			float temp1, temp2, temp3;
//			
//			float limit = optionsFloat[0]*optionsFloat[0];
//			
//			for (; pixelIt < optionsInt[OPTION_INT_MAX_ITERATIONS] ; pixelIt++){
//				
//				iterate(dataOffset);
////					
//				double z_r = data[IDX_Z_R + dataOffset];
//				double z_i = data[IDX_Z_I + dataOffset];
//				finished = z_r*z_r+z_i*z_i > limit;
//				if (finished){
////					pixelIt = (float) (pixelIt + 1 - Math.log(Math.log(data[IDX_Z_R + dataOffset]*data[IDX_Z_R + dataOffset]+data[IDX_Z_I + dataOffset]*data[IDX_Z_I + dataOffset])*0.5/Math.log(2))
////					/Math.log(Math.sqrt(data[IDX_SMOOTHSTEP_POW_R + dataOffset]*data[IDX_SMOOTHSTEP_POW_R + dataOffset]+data[IDX_SMOOTHSTEP_POW_I + dataOffset]*data[IDX_SMOOTHSTEP_POW_I + dataOffset]))); //TODO why 2*?
//
//					pixelIt = (float) (pixelIt + 1 - Math.log(Math.log(z_r*z_r+z_i*z_i)*0.5 / Math.log(2)) /smoothstepConstant); //TODO why 2*?
//					break;	
//				}
////				stop = finished;
//			}
////		}
//		if (finished){
//			resultsArr[i] = pixelIt;
//		}
//		else {
//			resultsArr[i] = -1;
//		}
//	}
//
//	@Override
//	public abstract void iterate(int dataOffset);
//
//	@Override
//	public void instr_square(int r1, int i1) {
//		double temp1;
//		temp1 = data[r1]*data[r1] - data[i1]*data[i1];
//		data[i1] = data[r1]*data[i1]*2;
//		data[r1] = temp1;
//	}
//
//	@Override
//	public void instr_sin_complex(int r1, int i1) {
//		double temp1;
//		temp1 = Math.sin(data[r1]) * Math.cosh(data[i1]);
//		data[i1] = Math.cos(data[r1]) * Math.sinh(data[i1]);
//		data[r1] = temp1;
//	}
//	
//	@Override
//	public void instr_sin_part(int p1) {
//		data[p1] = Math.sin(data[p1]);
//	}
//
//	@Override
//	public void instr_cos_complex(int r1, int i1) {
//		double temp1;
//		temp1 = Math.cos(data[r1]) * Math.cosh(data[i1]);
//		data[i1] = -Math.sin(data[r1]) * Math.sinh(data[i1]);
//		data[r1] = temp1;
//	}
//
//	@Override
//	public void instr_cos_part(int p1) {
//		data[p1] = Math.cos(data[p1]);
//	}
//	
//	@Override
//	public void instr_tan_complex(int r1, int i1) {
//		//tan(a + bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i
//		//https://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/complex/Complex.html#tan()
//		double temp1;
//		temp1 = Math.sin(2*data[r1])/(Math.cos(2*data[r1])+Math.cosh(2*data[i1]));
//		data[i1] = Math.sinh(2*data[i1])/(Math.cos(2*data[r1])+Math.cosh(2*data[i1]));
//		data[r1] = temp1;
//	}
//	
//	@Override
//	public void instr_tan_part(int p1) {
//		data[p1] = Math.tan(data[p1]);
//	}
//
//	@Override
//	public void instr_sinh_complex(int r1, int i1) {
//		//sinh(a + bi) = sinh(a)cos(b)) + cosh(a)sin(b)i
//		double temp1;
//		temp1 = Math.sinh(data[r1]) * Math.cos(data[i1]);
//		data[i1] = Math.cosh(data[r1]) * Math.sin(data[i1]);
//		data[r1] = temp1;
//	}
//
//	@Override
//	public void instr_sinh_part(int p1) {
//		data[p1] = Math.sinh(data[p1]);
//	}
//
//	@Override
//	public void instr_cosh_complex(int r1, int i1) {
//		//cosh(a + bi) = cosh(a)cos(b) + sinh(a)sin(b)i
//		double temp1;
//		temp1 = Math.cosh(data[r1]) * Math.cos(data[i1]);
//		data[i1] = Math.sinh(data[r1]) * Math.sin(data[i1]);
//		data[r1] = temp1;
//	}
//
//	@Override
//	public void instr_cosh_part(int p1) {
//		data[p1] = Math.cosh(data[p1]);
//	}
//	
//	@Override
//	public void instr_tanh_complex(int r1, int i1) {
//		//tanh(a + bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i
//		double temp1; 
//		temp1 = Math.sinh(2*data[r1])/(Math.cosh(2*data[r1])+Math.cos(2*data[i1]));
//		data[i1] = Math.sin(2*data[i1])/(Math.cosh(2*data[r1])+Math.cos(2*data[i1]));
//		data[r1] = temp1;
//	}
//	
//	@Override
//	public void instr_tanh_part(int p1) {
//		data[p1] = Math.tanh(data[p1]);
//	}
//
//	@Override
//	public void instr_abs_complex(int r1, int i1) {
//		data[r1] = Math.abs(data[r1]);
//		data[i1] = Math.abs(data[i1]);
//	}
//
//	@Override
//	public void instr_abs_part(int p1) {
//		data[p1] = Math.abs(data[p1]);
//	}
//
//	@Override
//	public void instr_copy_complex(int r1, int i1, int r2, int i2) {
//		data[r2] = data[r1];
//		data[i2] = data[i1];
//	}
//	
//	@Override
//	public void instr_copy_part(int p1, int p2){
//		data[p2] = data[p1];
//	}
//
//	@Override
//	public void instr_negate_complex(int r1, int i1) {
//		data[r1] = -data[r1];
//		data[i1] = -data[i1];
//	}
//
//	@Override
//	public void instr_negate_part(int p1) {
//		data[p1] = -data[p1];
//	}
//
//	@Override
//	public void instr_add_complex(int r1, int i1, int r2, int i2) {
//		data[r1] = data[r1]+data[r2];
//		data[i1] = data[i1]+data[i2];
//	}
//
//	@Override
//	public void instr_add_part(int p1, int p2) {
//		data[p1] = data[p1]+data[p2];
//	}
//
//	@Override
//	public void instr_sub_complex(int r1, int i1, int r2, int i2) {
//		data[r1] = data[r1]-data[r2];
//		data[i1] = data[i1]-data[i2];
//	}
//
//	@Override
//	public void instr_sub_part(int p1, int p2) {
//		data[p1] = data[p1]-data[p2];
//	}
//
//	@Override
//	public void instr_mult_complex(int r1, int i1, int r2, int i2) {
//		double temp1;
//		temp1 = data[r1]*data[r2] - data[i1]*data[i2];
//		data[i1] = (data[r1]+data[i1])*(data[r2]+data[i2]) - temp1;
//		data[r1] = temp1;
//	}
//
//	@Override
//	public void instr_mult_part(int p1, int p2) {
//		data[p1] = data[p1]*data[p2];
//	}
//	
//	@Override
//	public void instr_pow_complex_or_square(int r1, int i1, int r2, int i2){
//		if (data[r2] == 2f && data[i2] == 0f)
//			instr_square(r1, i1);
//		else
//			instr_pow_complex(r1, i1, r2, i2);
//	}
//	
//	@Override
//	public void instr_pow_complex(int r1, int i1, int r2, int i2){
//		double temp1;
//		double temp2;
//		double temp3;
//		temp3 = Math.sqrt(data[r1]*data[r1]+data[i1]*data[i1]);
//		if (temp3 != 0){
//			temp3 = Math.log(temp3);
//			temp1 = Math.atan2(data[i1], data[r1]);
//			//mult
//			temp2 = (temp3*data[r2] - temp1*data[i2]);
//			temp1 = (temp3*data[i2] + temp1*data[r2]);
//			temp3 = temp2;
//			//exp
//			temp2 = Math.exp(temp3);
//			data[r1] = temp2 * Math.cos(temp1);
//			data[i1] = temp2 * Math.sin(temp1);
//		} else {
//			data[r1] = data[r1]; //Workaround for "child list broken" in nested if-clause without else
//		}
//	}
//	
//	@Override
//	public void instr_pow_part(int p1, int p2){
//		data[p1] = Math.pow(data[p1], data[p2]);
//	}
}
