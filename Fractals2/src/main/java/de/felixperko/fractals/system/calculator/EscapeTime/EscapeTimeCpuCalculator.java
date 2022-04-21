package de.felixperko.fractals.system.calculator.EscapeTime;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import com.aparapi.internal.model.CacheEnabler;

import de.felixperko.expressions.ComputeExpressionBuilder;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.manager.server.ResourceManager;
import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeGpuCalculator.CalculatePhase;
import de.felixperko.fractals.system.calculator.infra.AbstractFractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.util.NumberUtil;

@Deprecated
public class EscapeTimeCpuCalculator
//extends AbstractFractalsCalculator
{

//	private static final long serialVersionUID = 1272085060796402662L;
//
//	static ThreadLocal<EscapeTimeCpuKernel> tl_kernel = new ThreadLocal<>();
//	static ThreadLocal<ComputeKernelParameters> tl_kernelParameters = new ThreadLocal<>();
//	
//	static EscapeTimeKernelFactoryAsm kernel_factory = new EscapeTimeKernelFactoryAsm();
//	
//	
//	int maxIterations;
//	int maxIterationsGlobal;
//	boolean storeEndResults;
//	
//	double limit;
//	int upsample;
//	AbstractArrayChunk chunk;
//	
//	Queue<Integer> redo = new LinkedList<Integer>();
//	
//	CalculatePhase phase = null;
//	
//	IStats taskStats;
//	
//	
//	
//	@Override
//	public void calculate(AbstractArrayChunk chunk, IStats taskStats, CalculateFractalsThread thread) {
//		
//		this.chunk = chunk;
//		this.taskStats = taskStats;
//		
//		limit = systemContext.getParamValue("limit", Double.class);
////		p_current = systemContext.getParameters().get(BFOrbitCommon.PARAM_ZSTART);
////		p_pow = systemContext.getParameters().get("pow");
////		p_c = systemContext.getParameters().get("c");
//		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();
//
//		upsample = layer.getUpsample();
//		maxIterations = layer.getMaxIterations();
//		maxIterationsGlobal = systemContext.getParamValue("iterations", Integer.class);
//		storeEndResults = maxIterations != -1 && maxIterations < maxIterationsGlobal;
//		if (maxIterations == -1) {
//			maxIterations = maxIterationsGlobal;
//		}
//
//		int pixelCount = chunk.getArrayLength();
//		
//		List<Layer> layers = systemContext.getLayerConfiguration().getLayers();
//		
//		EscapeTimeCpuKernel kernel = getCurrentKernel(pixelCount, layers);
//		
//		int samples = layer.getSampleCount();
//		int chunkSize = chunk.getChunkDimensions();
//		
//		prepareKernel(chunk, kernel, chunkSize, samples, layers.get(layers.size()-1).getSampleCount());
//		executeKernel(chunk, layer, samples, pixelCount, kernel);
//	}
//	
//	private EscapeTimeCpuKernel getCurrentKernel(int pixelCount, List<Layer> layers) {
//		
//		String new_expression = systemContext.getParamContainer().getClientParameter(BFOrbitCommon.PARAM_EXPRESSION).getGeneral(String.class);
//		
//		String inputVarName = null;
//		if (new_expression.contains("X")) inputVarName = "X";
//		if (new_expression.contains("x")) inputVarName = "x";
//		if (new_expression.contains("Z")) inputVarName = "Z";
//		if (new_expression.contains("z")) inputVarName = "z";
//		
//		ComputeExpressionBuilder expressionBuilder = new ComputeExpressionBuilder(new_expression, inputVarName, systemContext.getParamContainer().getClientParameters());
//		ComputeExpression expression = expressionBuilder.getComputeExpression();
//		
//		ComputeKernelParameters newKernelParameters = new ComputeKernelParameters(expression, layers, pixelCount, 64);
//		
//		ComputeKernelParameters kernelParameters = tl_kernelParameters.get();
//		if (kernelParameters == null || !kernelParameters.isCompartible(newKernelParameters, systemContext.getParamContainer())){
//			tl_kernelParameters.set(newKernelParameters);
//			EscapeTimeCpuKernel newKernel = kernel_factory.createKernelCpu(newKernelParameters);
//			tl_kernel.set(newKernel);
//		}
//		return tl_kernel.get();
//	}
//	
//	private void prepareKernel(AbstractArrayChunk chunk, EscapeTimeCpuKernel kernel, int chunkSize, int samples, int lastLayerSamples) {
//		kernel.optionsInt[0] = maxIterations;
//		kernel.optionsInt[1] = chunkSize;
//		kernel.optionsInt[2] = samples;
//		kernel.optionsFloat[0] = (float)(double)systemContext.getParamValue("limit", Double.class);
//
//		for (int i = 0 ; i < samples ; i++){
//			ComplexNumber offset = systemContext.getLayerConfiguration().getOffsetForSample(i);
//			kernel.sampleOffsets[i*2] = (float)offset.realDouble();
//			kernel.sampleOffsets[i*2+1] = (float)offset.imagDouble();
//		}
//		
//		paramLoop:
//			for (int i = 0 ; i < kernel.paramNames.length ; i++){
//				String name = kernel.paramNames[i];
//				ParamSupplier supp = systemContext.getParamContainer().getClientParameter(name);
//				
//				if (supp == null){
//					//search constant
//					supp = kernel.constantSuppliers.get(name);
//					//no constant found -> missing supplier
//					if (supp == null)
//						throw new IllegalStateException("Missing ParamSupplier "+name);
//				}
//				
//				if (supp instanceof StaticParamSupplier){
//					ComplexNumber val = (ComplexNumber)((StaticParamSupplier)supp).getObj();
//					kernel.params[i*3] = (float) val.realDouble();
//					kernel.params[i*3+1] = (float) val.imagDouble();
//					kernel.params[i*3+2] = 0;
//				}
//				else if (supp instanceof CoordinateBasicShiftParamSupplier){
//					CoordinateBasicShiftParamSupplier shiftSupp = (CoordinateBasicShiftParamSupplier) supp;
//					kernel.params[i*3] = chunk.chunkPos.realDouble();
//					kernel.params[i*3+1] = chunk.chunkPos.imagDouble();
//					kernel.params[i*3+2] = systemContext.getPixelzoom().toDouble();
//				}
//				else
//					throw new IllegalArgumentException("Unsupported ParamSupplier "+supp.getName()+": "+supp.getClass().getName());
//			}
//	}
//
//	private void executeKernel(AbstractArrayChunk chunk, Layer layer, int samples, int pixelCount,
//			EscapeTimeCpuKernel kernel) {
//		phase = CalculatePhase.PHASE_MAINLOOP;
//		
//		//get max amount of samples that have to be completed for any pixel
//		
//		int batchSize = 256;
//			
//			loop : 
//				for (int pixel = 0 ; pixel < pixelCount ; pixel++) {
//					
//					if (isCancelled())
//						break;
//					
//					boolean pixelActive = layer.isActive(pixel);
//					if (!pixelActive){
////						if (layer instanceof BreadthFirstUpsampleLayer)
////							pixel = ((BreadthFirstUpsampleLayer)layer).getEnabledPixels().nextEnabled(pixel)-1;
////						if (pixel == -2)
////							break;
//						continue;
//					}
//					int currentSamples = chunk.getSampleCount(pixel);
//					kernel.pixelSamples[pixel] = currentSamples;
//					
//					for (int sample = currentSamples ; sample < samples ; sample++) {
//					
////						kernel.pixelWasSampled[pixel] = 0;
//						kernel.pixelItArr[pixel] = 0;
//						kernel.resultsArr[pixel] = -1;
//	
////						kernel.run(pixel, pixel%batchSize);
//						kernel.run(pixel, 0);
//						float result = kernel.resultsArr[pixel]+1;
////						if (chunk.getChunkX() == 1 && chunk.getChunkY() == 0 && pixel == 0){
////							System.out.println(result);
////						}
//						
//						int sucessfulSamples = kernel.pixelWasSampled[pixel];
////						if (sucessfulSamples > 0){
//							chunk.addSample(pixel, result, upsample);
//							sample_success(pixel);
////						}
//					}
//				}
////		}
////		for (int sample = currentMinSamples ; sample < samples ; sample++){
////			boolean noChange = true;
////			if (isCancelled())
////				break;
////			
////			int count = (int) (Math.ceil(pixelCount/(double)batchSize));
////			
////			for (int i = 0 ; i < count ; i++){
////				
////				int loopCount = batchSize;
////				if (i*batchSize+loopCount > pixelCount)
////					loopCount = pixelCount-i*batchSize;
////				
////				int pixelOffset = i*batchSize;
////				
////				for (int j = 0 ; j < loopCount ; j++){
////					int px = pixelOffset+j;
////					if (layer.isActive(px))
////						kernel.pixelSamples[px] = chunk.getSampleCount(px);
////					else
////						kernel.pixelSamples[px] = samples;
////					kernel.pixelWasSampled[px] = 0;
////				}
////				
////				for (int j = 0 ; j < loopCount ; j++){
////					kernel.run(pixelOffset+j, j);
////				}
////				
////	//				kernel.get(resultsArr);
////				
////				for (int j = 0 ; j < loopCount ; j++){
////					int px = i*batchSize+j;
////					int sucessfulSamples = kernel.pixelWasSampled[px];
////					if (sucessfulSamples > 0){
////						chunk.addSample(px, kernel.resultsArr[px], upsample);
////						sample_success(px);
////						noChange = false;
////					}
////				}
////				for (int j = 0 ; j < kernel.resultsArr.length ; j++)
////					kernel.resultsArr[j] = 0;
////				if (noChange)
////					break;
////			}
////		}
//	}
//	private void sample_success(int pixel) {
////		if (chunk.getValue(pixel, true) <= 0)
//			sample_first_success(pixel);
//			
//	}
//	
//	private void sample_first_success(int pixel) {
//		final int chunkDimensions = chunk.getChunkDimensions();
//		final int upsample = chunk.getUpsample();
//		final int chunkArrayLength = chunk.getArrayLength();
//		int thisX = pixel / chunkDimensions;
//		int thisY = pixel % chunkDimensions;
//		//disable culling of surrounding pixels
//		for (int dx = -1 ; dx <= 1 ; dx++) {
//			for (int dy = -1 ; dy <= 1 ; dy++) {
//				
//				if (dx == 0 && dy == 0)
//					continue;
//				
//				int x = thisX + dx*upsample;
//				int y = thisY + dy*upsample;
//				
//				//determine if in neighbour chunk
//				boolean local = true;
//				if (x < 0){
//					int low = thisY - thisY % (upsample);
//					int high = low+upsample-1;
//					local = false;
//					if (y > 0 && y < chunkDimensions)
//						chunk.getBorderData(BorderAlignment.LEFT).set(true, low, high);
//				} else if (x >= chunkDimensions){
//					int low = thisY - thisY % (upsample);
//					int high = low+upsample-1;
//					local = false;
//					if (y > 0 && y < chunkDimensions)
//						chunk.getBorderData(BorderAlignment.RIGHT).set(true, low, high);
//				}
//				
//				if (y < 0){
//					int low = thisX - thisX % (upsample);
//					int high = low+upsample-1;
//					local = false;
//					if (x > 0 && x < chunkDimensions)
//						chunk.getBorderData(BorderAlignment.UP).set(true, low, high);
//				} else if (y >= chunkDimensions){
//					int low = thisX - thisX % (upsample);
//					int high = low+upsample-1;
//					local = false;
//					if (x > 0 && x < chunkDimensions)
//						chunk.getBorderData(BorderAlignment.DOWN).set(true, low, high);
//				}
//				
//				//local -> add to redo queue
//				if (local){
//					int pixel2 = x*chunkDimensions + y;
//					if (chunk.getValue(pixel2, true) == AbstractArrayChunk.FLAG_CULL) {
//						chunk.setCullFlags(pixel2, 1, false);
//						if (phase == CalculatePhase.PHASE_REDO || pixel > pixel2) {
//							redo.add(pixel2);
//						}
//					}
//				}
//			}
//		}
//	}
}
