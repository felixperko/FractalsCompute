package de.felixperko.fractals.system.calculator.EscapeTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public abstract class EscapeTimeCpuKernelNew implements ComputeKernel {

	double data[];
	ComputeKernelParameters kernelParameters;
	String expression;
	Map<String, ParamSupplier> constantSuppliers;
//	double[] constants;
	String[] paramNames;
	double smoothstepConstant;
	
    int traceArrayFilledSize = 0;
    int tracesCount = 0;
    double[] tracesReal = null;
    double[] tracesImag = null;
	
	public EscapeTimeCpuKernelNew(ComputeKernelParameters kernelParameters) {
		
		this.kernelParameters = kernelParameters;
		this.expression = kernelParameters.getExpression().getText();
		
		List<ParamSupplier> paramSuppliers = kernelParameters.getExpression().getParameterList();
		constantSuppliers = kernelParameters.getExpression().getConstants();
		
		int pixelCount = kernelParameters.getPixelCount();
//		pixelItArr = new int[pixelCount];
//		resultsArr = new float[pixelCount];
//		pixelSamples = new int[pixelCount];
//		pixelWasSampled = new int[pixelCount];
//		
//		params = new double[paramSuppliers.size()*3];
		paramNames = new String[paramSuppliers.size()];
		for (int i = 0 ; i < paramSuppliers.size() ; i++)
			paramNames[i] = paramSuppliers.get(i).getName();
		List<Layer> layers = kernelParameters.getLayers();
//		sampleOffsets = new float[layers.get(layers.size()-1).getSampleCount()*2];
		
//		this.constantNames = new String[constantNames.size()];
//		this.constants = new double[constantSuppliers.size()*2];
		this.data = new double[kernelParameters.getExpression().getRequiredVariableSlots()*2];
		
		this.smoothstepConstant = kernelParameters.getExpression().getSmoothstepConstant();
	}
	
	public void setTraces(int count){
		this.tracesCount = count;
	}
	
	public double[][] getTraces(){
		return new double[][]{tracesReal, tracesImag};
	}
	
	public float run(int pixel, double[] params, float[] sampleOffsets, int chunkSize, int currentSamples, int maxIterations, double limitSq) {
		
		float x = (pixel / chunkSize) + sampleOffsets[currentSamples*2];
		float y = (pixel % chunkSize) + sampleOffsets[currentSamples*2+1];
		float result = 0;
		
		for (int j = 0 ; j < params.length/3 ; j++){
			data[j*2+0] = (params[j*3+0] + x * params[j*3+2]);
			data[j*2+1] = (params[j*3+1] + y * params[j*3+2]);
		}
		for (int j = params.length/3 ; j < data.length/2 ; j++){
			data[j*2+0] = 0;
			data[j*2+1] = 0;
		}
		
		if (tracesCount > 0){
			tracesReal = new double[tracesCount];
			tracesImag = new double[tracesCount];
		}
		
		boolean finished = false;
		for (int pixelIt = 0; pixelIt < maxIterations ; pixelIt++){

			//trace
			if (pixelIt < tracesCount){
				tracesReal[pixelIt] = data[0];
				tracesImag[pixelIt] = data[1];
			}
			
			iterate(0);
			
			//check condition
			finished = data[0]*data[0] + data[1]*data[1] > limitSq;
			if (finished){
				result = (float) (pixelIt + 1 - Math.log(Math.log(data[0]*data[0] + data[1]*data[1])*0.5 / Math.log(2)) /smoothstepConstant);
				if (pixelIt < tracesCount && pixelIt < maxIterations-1){

					tracesReal[pixelIt] = data[0];
					tracesImag[pixelIt] = data[1];
				}
				break;
			}
		}
		
		if (finished)
			return result;
		return -1;
		
	}

	@Override
	public abstract void iterate(int dataOffset);

	@Override
	public void instr_square(int r1, int i1) {
		double temp1;
		temp1 = data[r1]*data[r1] - data[i1]*data[i1];
		data[i1] = data[r1]*data[i1]*2;
		data[r1] = temp1;
	}

	@Override
	public void instr_sin_complex(int r1, int i1) {
		double temp1;
		temp1 = Math.sin(data[r1]) * Math.cosh(data[i1]);
		data[i1] = Math.cos(data[r1]) * Math.sinh(data[i1]);
		data[r1] = temp1;
	}
	
	@Override
	public void instr_sin_part(int p1) {
		data[p1] = Math.sin(data[p1]);
	}

	@Override
	public void instr_cos_complex(int r1, int i1) {
		double temp1;
		temp1 = Math.cos(data[r1]) * Math.cosh(data[i1]);
		data[i1] = -Math.sin(data[r1]) * Math.sinh(data[i1]);
		data[r1] = temp1;
	}

	@Override
	public void instr_cos_part(int p1) {
		data[p1] = Math.cos(data[p1]);
	}
	
	@Override
	public void instr_tan_complex(int r1, int i1) {
		//tan(a + bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i
		//https://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/complex/Complex.html#tan()
		double temp1;
		temp1 = Math.sin(2*data[r1])/(Math.cos(2*data[r1])+Math.cosh(2*data[i1]));
		data[i1] = Math.sinh(2*data[i1])/(Math.cos(2*data[r1])+Math.cosh(2*data[i1]));
		data[r1] = temp1;
	}
	
	@Override
	public void instr_tan_part(int p1) {
		data[p1] = Math.tan(data[p1]);
	}

	@Override
	public void instr_sinh_complex(int r1, int i1) {
		//sinh(a + bi) = sinh(a)cos(b)) + cosh(a)sin(b)i
		double temp1;
		temp1 = Math.sinh(data[r1]) * Math.cos(data[i1]);
		data[i1] = Math.cosh(data[r1]) * Math.sin(data[i1]);
		data[r1] = temp1;
	}

	@Override
	public void instr_sinh_part(int p1) {
		data[p1] = Math.sinh(data[p1]);
	}

	@Override
	public void instr_cosh_complex(int r1, int i1) {
		//cosh(a + bi) = cosh(a)cos(b) + sinh(a)sin(b)i
		double temp1;
		temp1 = Math.cosh(data[r1]) * Math.cos(data[i1]);
		data[i1] = Math.sinh(data[r1]) * Math.sin(data[i1]);
		data[r1] = temp1;
	}

	@Override
	public void instr_cosh_part(int p1) {
		data[p1] = Math.cosh(data[p1]);
	}
	
	@Override
	public void instr_tanh_complex(int r1, int i1) {
		//tanh(a + bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i
		double temp1; 
		temp1 = Math.sinh(2*data[r1])/(Math.cosh(2*data[r1])+Math.cos(2*data[i1]));
		data[i1] = Math.sin(2*data[i1])/(Math.cosh(2*data[r1])+Math.cos(2*data[i1]));
		data[r1] = temp1;
	}
	
	@Override
	public void instr_tanh_part(int p1) {
		data[p1] = Math.tanh(data[p1]);
	}

	@Override
	public void instr_abs_complex(int r1, int i1) {
		data[r1] = Math.abs(data[r1]);
		data[i1] = Math.abs(data[i1]);
	}

	@Override
	public void instr_abs_part(int p1) {
		data[p1] = Math.abs(data[p1]);
	}

	@Override
	public void instr_copy_complex(int r1, int i1, int r2, int i2) {
		data[r2] = data[r1];
		data[i2] = data[i1];
	}
	
	@Override
	public void instr_copy_part(int p1, int p2){
		data[p2] = data[p1];
	}

	@Override
	public void instr_negate_complex(int r1, int i1) {
		data[r1] = -data[r1];
		data[i1] = -data[i1];
	}

	@Override
	public void instr_negate_part(int p1) {
		data[p1] = -data[p1];
	}

	@Override
	public void instr_add_complex(int r1, int i1, int r2, int i2) {
		data[r1] = data[r1]+data[r2];
		data[i1] = data[i1]+data[i2];
	}

	@Override
	public void instr_add_part(int p1, int p2) {
		data[p1] = data[p1]+data[p2];
	}

	@Override
	public void instr_sub_complex(int r1, int i1, int r2, int i2) {
		data[r1] = data[r1]-data[r2];
		data[i1] = data[i1]-data[i2];
	}

	@Override
	public void instr_sub_part(int p1, int p2) {
		data[p1] = data[p1]-data[p2];
	}

	@Override
	public void instr_mult_complex(int r1, int i1, int r2, int i2) {
		double temp1;
		temp1 = data[r1]*data[r2] - data[i1]*data[i2];
		data[i1] = (data[r1]+data[i1])*(data[r2]+data[i2]) - temp1;
		data[r1] = temp1;
	}

	@Override
	public void instr_mult_part(int p1, int p2) {
		data[p1] = data[p1]*data[p2];
	}
	
	@Override
	public void instr_pow_complex_or_square(int r1, int i1, int r2, int i2){
		if (data[r2] == 2f && data[i2] == 0f)
			instr_square(r1, i1);
		else
			instr_pow_complex(r1, i1, r2, i2);
	}
	
	@Override
	public void instr_pow_complex(int r1, int i1, int r2, int i2){
		double temp1;
		double temp2;
		double temp3;
		temp3 = Math.sqrt(data[r1]*data[r1]+data[i1]*data[i1]);
		if (temp3 != 0){
			temp3 = Math.log(temp3);
			temp1 = Math.atan2(data[i1], data[r1]);
			//mult
			temp2 = (temp3*data[r2] - temp1*data[i2]);
			temp1 = (temp3*data[i2] + temp1*data[r2]);
			temp3 = temp2;
			//exp
			temp2 = Math.exp(temp3);
			data[r1] = temp2 * Math.cos(temp1);
			data[i1] = temp2 * Math.sin(temp1);
		} else {
			data[r1] = data[r1]; //Workaround for "child list broken" in nested if-clause without else
		}
	}
	
	@Override
	public void instr_pow_part(int p1, int p2){
		data[p1] = Math.pow(data[p1], data[p2]);
	}

}
