package de.felixperko.fractals.system.calculator.EscapeTime;

import java.util.List;
import com.aparapi.device.Device;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.task.Layer;

public abstract class EscapeTimeGpuKernel32 extends EscapeTimeGpuKernelAbstract{

	static List<Device> suitable_devices;
	
	@Local final float[] data;

	static final int LOCAL_SIZE = 256;
	
	//
	//CPU Code
	//
	
	protected EscapeTimeGpuKernel32(Device device, ComputeKernelParameters kernelParameters){
		super(device, kernelParameters);
		
//		for (int i = 0 ; i < constantNames.size() ; i++){
//			String constantName = constantNames.get(i);
//			this.constantNames[i] = constantName;
//			ComplexNumber value = paramContainer.getClientParameter(constantName).getGeneral(ComplexNumber.class);
//			this.constants[i*2  ] = value.realDouble();
//			this.constants[i*2+1] = value.imagDouble();
//		}
		
		List<ComputeInstruction> instructions = kernelParameters.getMainExpression().getInstructions();
		
		int dataBufferSize = kernelParameters.getMainExpression().getRequiredVariableSlots();
		int instructionsBufferSize = 2+instructions.size();
		this.instructions = new int[instructionsBufferSize];
		this.instructions[0] = instructionsBufferSize;
		this.instructions[1] = dataBufferSize;
		
		data = new float[LOCAL_SIZE*dataBufferSize];
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

//			pixelSamples[i] = pixelSamples[i] + 1;
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
			float smoothstepConstant = optionsFloat[1];
			
			while (!stop){
				
				iterate(dataOffset);
//					
				//loop handling
				pixelIt++;
				finished = data[IDX_Z_R + dataOffset]*data[IDX_Z_R + dataOffset]+data[IDX_Z_I + dataOffset]*data[IDX_Z_I + dataOffset] > limit;
				stop = finished || pixelIt >= optionsInt[0];
			}
			if (finished){
				//implicit pixelIt -= 1;
				pixelIt = (float) (pixelIt - log(log(data[IDX_Z_R + dataOffset]*data[IDX_Z_R + dataOffset]+data[IDX_Z_I + dataOffset]*data[IDX_Z_I + dataOffset])*0.5/log(2))
					/smoothstepConstant);
				resultsArr[i] = pixelIt;
				
			}
			else {
				resultsArr[i] = -1;
			}
		}
	}

	protected abstract void iterate(int dataOffset);

	protected void instr_square(int r1, int i1) {
		float temp1;
		temp1 = data[r1]*data[r1] - data[i1]*data[i1];
		data[i1] = data[r1]*data[i1]*2;
		data[r1] = temp1;
	}

	protected void instr_sin_complex(int r1, int i1) {
		float temp1;
		temp1 = sin(data[r1]) * cosh(data[i1]);
		data[i1] = cos(data[r1]) * sinh(data[i1]);
		data[r1] = temp1;
	}

	protected void instr_sin_part(int p1) {
		data[p1] = sin(data[p1]);
	}

	protected void instr_cos_complex(int r1, int i1) {
		float temp1;
		temp1 = cos(data[r1]) * cosh(data[i1]);
		data[i1] = -sin(data[r1]) * sinh(data[i1]);
		data[r1] = temp1;
	}

	protected void instr_cos_part(int p1) {
		data[p1] = cos(data[p1]);
	}
	
	protected void instr_tan_complex(int r1, int i1) {
		//tan(a + bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i
		//https://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/complex/Complex.html#tan()
		float temp1;
		temp1 = sin(2*data[r1])/(cos(2*data[r1])+cosh(2*data[i1]));
		data[i1] = sinh(2*data[i1])/(cos(2*data[r1])+cosh(2*data[i1]));
		data[r1] = temp1;
	}
	
	protected void instr_tan_part(int p1) {
		data[p1] = tan(data[p1]);
	}

	protected void instr_sinh_complex(int r1, int i1) {
		//sinh(a + bi) = sinh(a)cos(b)) + cosh(a)sin(b)i
		float temp1;
		temp1 = sinh(data[r1]) * cos(data[i1]);
		data[i1] = cosh(data[r1]) * sin(data[i1]);
		data[r1] = temp1;
	}

	protected void instr_sinh_part(int p1) {
		data[p1] = sinh(data[p1]);
	}

	protected void instr_cosh_complex(int r1, int i1) {
		//cosh(a + bi) = cosh(a)cos(b) + sinh(a)sin(b)i
		float temp1;
		temp1 = cosh(data[r1]) * cos(data[i1]);
		data[i1] = sinh(data[r1]) * sin(data[i1]);
		data[r1] = temp1;
	}

	protected void instr_cosh_part(int p1) {
		data[p1] = cosh(data[p1]);
	}
	
	protected void instr_tanh_complex(int r1, int i1) {
		//tanh(a + bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i
		float temp1; 
		temp1 = sinh(2*data[r1])/(cosh(2*data[r1])+cos(2*data[i1]));
		data[i1] = sin(2*data[i1])/(cosh(2*data[r1])+cos(2*data[i1]));
		data[r1] = temp1;
	}
	
	protected void instr_tanh_part(int p1) {
		data[p1] = tanh(data[p1]);
	}

	protected void instr_abs_complex(int r1, int i1) {
		data[r1] = abs(data[r1]);
		data[i1] = abs(data[i1]);
	}

	protected void instr_abs_part(int p1) {
		data[p1] = abs(data[p1]);
	}

	protected void instr_copy_complex(int r1, int i1, int r2, int i2) {
		data[r2] = data[r1];
		data[i2] = data[i1];
	}
	
	protected void instr_copy_part(int p1, int p2){
		data[p2] = data[p1];
	}

	protected void instr_negate_complex(int r1, int i1) {
		data[r1] = -data[r1];
		data[i1] = -data[i1];
	}

	protected void instr_negate_part(int p1) {
		data[p1] = -data[p1];
	}

	protected void instr_add_complex(int r1, int i1, int r2, int i2) {
		data[r1] = data[r1]+data[r2];
		data[i1] = data[i1]+data[i2];
	}

	protected void instr_add_part(int p1, int p2) {
		data[p1] = data[p1]+data[p2];
	}

	protected void instr_sub_complex(int r1, int i1, int r2, int i2) {
		data[r1] = data[r1]-data[r2];
		data[i1] = data[i1]-data[i2];
	}

	protected void instr_sub_part(int p1, int p2) {
		data[p1] = data[p1]-data[p2];
	}

	protected void instr_mult_complex(int r1, int i1, int r2, int i2) {
		float temp1;
		temp1 = data[r1]*data[r2] - data[i1]*data[i2];
		data[i1] = data[r1]*data[i2] + data[i1]*data[r2];
		data[r1] = temp1;
	}

	protected void instr_mult_part(int p1, int p2) {
		data[p1] = data[p1]*data[p2];
	}
	
	protected void instr_pow_complex_or_square(int r1, int i1, int r2, int i2){
		if (data[r2] == 2f && data[i2] == 0f)
			instr_square(r1, i1);
		else
			instr_pow_complex(r1, i1, r2, i2);
	}
	
	protected void instr_pow_complex(int r1, int i1, int r2, int i2){
		float temp1;
		float temp2;
		float temp3;
		temp3 = sqrt(data[r1]*data[r1]+data[i1]*data[i1]);
		if (temp3 != 0f){
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
	
	protected void instr_pow_part(int p1, int p2){
		data[p1] = pow(data[p1], data[p2]);
	}
	
	protected void instr_reciprocal_complex(int r1, int i1){
		float temp = data[r1]*data[r1] + data[i1]*data[i1];
		if (temp != 0f){
			data[r1] =   data[r1]/temp;
			data[i1] = - data[i1]/temp;
		}
	}
	
	protected void instr_reciprocal_part(int p1){
		data[p1] = 1f/data[p1];
	}
	
	protected void instr_log_part(int p1) {
		data[p1] = log(data[p1]);
	}
	
	protected void instr_log_complex(int r1, int i1) {
		//log(z) = ln(sqrt(r^2+i^2)) + i * atan2(i, r)
		float newI = atan2(data[i1], data[r1]);
		data[r1] = data[r1]*data[r1];
		data[i1] = data[i1]*data[i1];
		data[r1] = log(sqrt(data[r1]+data[i1]));
		data[i1] = newI;
	}

}
