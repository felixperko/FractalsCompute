package de.felixperko.fractals.system.calculator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import com.aparapi.Kernel;
import com.aparapi.device.Device;
import com.aparapi.device.Device.TYPE;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public class GPUKernel extends Kernel {
	
	static int TEST_COUNTER = 0;
	
	final Device device;
	
//	final int[] pixelsArr = new int[pixelCount];
	final int[] pixelItArr;
//	final float[] currentRealArr = new float[pixelCount];
//	final float[] currentImagArr = new float[pixelCount];
//	final float[] powRealArr = new float[pixelCount];
//	final float[] powImagArr = new float[pixelCount];
//	final float[] cRealArr = new float[pixelCount];
//	final float[] cImagArr = new float[pixelCount];
	final float[] resultsArr;
	final int[] pixelSamples;
	final int[] pixelWasSampled;
	
	final double[] params;
	final float[] sampleOffsets;
	
	final int[] optionsInt = new int[4]; //maxIterations, chunkSize, sampleCount
	final float[] optionsFloat = new float[1]; //bailout
	
	public static GPUKernel createKernel(List<ParamSupplier> paramSuppliers, List<Layer> layers, int pixelCount){
		
		int id = TEST_COUNTER++;
		
		KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
		List<Device> devices = new ArrayList<>(preferences.getPreferredDevices(null));
		Iterator<Device> it = devices.iterator();
		while (it.hasNext()){
			if (it.next().getType() != TYPE.GPU)
				it.remove();
		}
		int device = id % devices.size();
		
		List<Device> deviceList = new ArrayList<>(devices.subList(device, device+1)); 
		KernelManager.instance().setDefaultPreferredDevices(new LinkedHashSet<>(deviceList));
		
		GPUKernel kernel = new GPUKernel(deviceList.get(0), paramSuppliers, layers, pixelCount);
		KernelManager.instance().setPreferredDevices(kernel, new LinkedHashSet<>(deviceList));
		return kernel;
	}
	
	private GPUKernel(Device device, List<ParamSupplier> paramSuppliers, List<Layer> layers, int pixelCount){
		
		this.device = device;
		
		pixelItArr = new int[pixelCount];
		resultsArr = new float[pixelCount];
		pixelSamples = new int[pixelCount];
		pixelWasSampled = new int[pixelCount];
		
		params = new double[paramSuppliers.size()*3];
		sampleOffsets = new float[layers.get(layers.size()-1).getSampleCount()*2];
	}
		

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
			
			boolean hardcoded = false;
			boolean doublePrecision = false;
			
			if (hardcoded && doublePrecision){
				double real = (params[0] + x * params[2]);
				double imag = (params[1] + y * params[2]);
				
				double powReal = (params[3] + x * params[5]);
				double powImag = (params[4] + y * params[5]);
	
				double cReal = (params[6] + x * params[8]);
				double cImag = (params[7] + y * params[8]);
				
				while (!stop){
					
//					real = abs(real);
//					imag = abs(imag);
	
	//				real = -real;
	//				imag = -imag;
					
					if (powReal == 2 && powImag == 0){
						//square
						double temp = real*real - imag*imag;
						imag = real*imag*2;
						real = temp;
					} else {
						double abs = sqrt(real*real+imag*imag);
						double logAbs = 0;
						if (abs != 0){
							logAbs = log(abs);
							double angle = atan2(imag, real);
							//mult
							double temp = (logAbs*powReal - angle*powImag);
							angle = (logAbs*powImag + angle*powReal);
							logAbs = temp;
							//exp
							double factor = exp(logAbs);
							real = factor * cos(angle);
							imag = factor * sin(angle);
						}
					}
					
					//add
					real += cReal;
					imag += cImag;
					
					//loop handling
					pixelIt++;
					finished = real*real+imag*imag > optionsFloat[0];
					if (finished)
						pixelIt = (float) (pixelIt + 1 - log(log(real*real+imag*imag)*0.5/log(2))/log(sqrt(powReal*powReal+powImag*powImag)));
					
					stop = finished || pixelIt >= optionsInt[0];
					
				}
			} else if (hardcoded && !doublePrecision) {
				float real = (float)(params[0] + x * params[2]);
				float imag = (float)(params[1] + y * params[2]);
				
				float powReal = (float)(params[3] + x * params[5]);
				float powImag = (float)(params[4] + y * params[5]);
	
				float cReal = (float)(params[6] + x * params[8]);
				float cImag = (float)(params[7] + y * params[8]);
				
				while (!stop){
					
//					float temp_ = real;
//					real = imag;
//					imag = temp_;
					
//					real = abs(real);
					imag = abs(imag);
	
//					real = -real;
//					imag = -imag;
					
					if (powReal == 2 && powImag == 0){
						//square
						float temp = real*real - imag*imag;
						imag = real*imag*2;
						real = temp;
					} else {
						float abs = sqrt(real*real+imag*imag);
						float logAbs = 0;
						if (abs != 0){
							logAbs = log(abs);
							float angle = atan2(imag, real);
							//mult
							float temp = (logAbs*powReal - angle*powImag);
							angle = (logAbs*powImag + angle*powReal);
							logAbs = temp;
							//exp
							float factor = exp(logAbs);
							real = factor * cos(angle);
							imag = factor * sin(angle);
						}
					}
					
					//add
					real += cReal;
					imag += cImag;
					
					//loop handling
					pixelIt++;
					finished = real*real+imag*imag > optionsFloat[0];
					if (finished)
						pixelIt = (float) (pixelIt + 1 - log(log(real*real+imag*imag)*0.5/log(2))/log(sqrt(powReal*powReal+powImag*powImag)));
					
					stop = finished || pixelIt >= optionsInt[0];
					
				}
			}
			else if (!hardcoded){
				int dataOffset = getLocalId()*instructions[1];
				data[DATA_CURR_R + dataOffset] = (float)(params[0] + x * params[2]);
				data[DATA_CURR_I + dataOffset] = (float)(params[1] + y * params[2]);
				
				data[DATA_POW_R + dataOffset] = (float)(params[3] + x * params[5]);
				data[DATA_POW_I + dataOffset] = (float)(params[4] + y * params[5]);
	
				data[DATA_ADD_R + dataOffset] = (float)(params[6] + x * params[8]);
				data[DATA_ADD_I + dataOffset] = (float)(params[7] + y * params[8]);
				
//				data[7 + dataOffset] = (float)(3);
//
				
				int r1, i1, r2, i2;
				
				float temp1, temp2, temp3;
				
				while (!stop){
					
					for (int idx = 2 ; idx < instructions[0] ; idx++){
						
						final int expr = instructions[idx];
						r1 = bufferIdx[idx*4-8] + dataOffset;
						i1 = bufferIdx[idx*4-7] + dataOffset;
						r2 = bufferIdx[idx*4-6] + dataOffset;
						i2 = bufferIdx[idx*4-5] + dataOffset;
						
						if (expr == EXPR_POW){
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
								data[r1] = data[r1]; //Does nothing, else needed to compile GPU bytecode
							}
						}
						else if (expr == EXPR_SQUARE){
							temp1 = data[r1]*data[r1] - data[i1]*data[i1];
							data[i1] = data[r1]*data[i1]*2;
							data[r1] = temp1;
							
						}
						else if (expr == EXPR_ADD){
							data[r1] = data[r1]+data[r2];
							data[i1] = data[i1]+data[i2];
							
						}
						else if (expr == EXPR_WRITE_COMPLEX){
							data[r2] = data[r1];
							data[i2] = data[i1];
							
						}
////						else if (expr == EXPR_DIV){
////							//TODO
////							
////						}
//						else
						else if (expr == EXPR_ABS){
							data[r1] = abs(data[r1]);
							data[i1] = abs(data[i1]);
						}
//						} else if (expr == EXPR_ABS_IMAG){
//							data[i1] = abs(data[i1]);
//							
//						} else if (expr == EXPR_SIN){
//							float temp = sin(data[r1]) * cosh(data[i1]);
//							data[i1] = cos(data[r1]) * sinh(data[i1]);
//							data[r1] = temp;
//							
//						}
//						else if (expr == EXPR_COS){
//							//TODO
//							
//						}
					}
//					
					//loop handling
					pixelIt++;
					finished = data[DATA_CURR_R + dataOffset]*data[DATA_CURR_R + dataOffset]+data[DATA_CURR_I + dataOffset]*data[DATA_CURR_I + dataOffset] > optionsFloat[0];
					if (finished)
						pixelIt = (float) (pixelIt + 1 - log(log(data[DATA_CURR_R + dataOffset]*data[DATA_CURR_R + dataOffset]+data[DATA_CURR_I + dataOffset]*data[DATA_CURR_I + dataOffset])*0.5/log(2))
						/log(sqrt(data[DATA_POW_R + dataOffset]*data[DATA_POW_R + dataOffset]+data[DATA_POW_I + dataOffset]*data[DATA_POW_I + dataOffset])));
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
	}
	
	static final int LOCAL_SIZE = 256;
	
	static final int DATA_CURR_R = 0;
	static final int DATA_CURR_I = 1;
	static final int DATA_POW_R = 2;
	static final int DATA_POW_I = 3;
	static final int DATA_ADD_R = 4;
	static final int DATA_ADD_I = 5;
	
	static final int EXPR_POW = 0;
	static final int EXPR_SQUARE = 1;
	static final int EXPR_ADD = 2;
	static final int EXPR_WRITE_COMPLEX = 3;
	static final int EXPR_DIV = 4;
	static final int EXPR_ABS = 5;
	static final int EXPR_ABS_IMAG = 6;
	static final int EXPR_SIN = 7;
	static final int EXPR_COS = 8;
	static final int EXPR_WRITE_REAL = 9;
	static final int EXPR_WRITE_IMAG = 10;
	
	int dataBufferSize = 6;
	
	int[] instructions = new int[]{4, dataBufferSize, EXPR_SQUARE, EXPR_ADD}; //0: instructionsLength; 1: dataBufferSize; [instr1, instr2,...]
	int[] bufferIdx = new int[]{DATA_CURR_R,DATA_CURR_I , DATA_POW_R,DATA_POW_I,  DATA_CURR_R,DATA_CURR_I , DATA_ADD_R,DATA_ADD_I}; // [{r1, i1, r2, i2},...]
	@Local float[] data = new float[LOCAL_SIZE*dataBufferSize];
	
//	public void evaluateExpressions(int dataOffset){
//		
//	}
	
	public Device getDevice(){
		return device;
	}

}
