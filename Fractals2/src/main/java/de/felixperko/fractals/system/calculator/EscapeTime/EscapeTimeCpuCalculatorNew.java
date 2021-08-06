package de.felixperko.fractals.system.calculator.EscapeTime;

import java.util.List;

import de.felixperko.expressions.ComputeExpressionBuilder;
import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.calculator.infra.AbstractFractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.common.BFOrbitCommon;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.system.numbers.Number;

public class EscapeTimeCpuCalculatorNew extends AbstractFractalsCalculator{
	
	static EscapeTimeKernelFactoryAsm kernel_factory = new EscapeTimeKernelFactoryAsm();

	static ThreadLocal<EscapeTimeCpuKernelNew> tl_kernel = new ThreadLocal<>();
	static ThreadLocal<ComputeKernelParameters> tl_kernelParameters = new ThreadLocal<>();
	
	public EscapeTimeCpuCalculatorNew(){
		
	}

	@Override
	public void calculate(AbstractArrayChunk chunk, IStats taskStats, CalculateFractalsThread thread) {

		if (systemContext == null)
			throw new IllegalStateException("tried to use calculator while systemContext was null. Call setContext() first.");
		
		EscapeTimeCpuKernelNew kernel = getCurrentKernel();
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();
		int samples = layer.getSampleCount();
		
		int upsample = layer.getUpsample();
		int maxIterations = layer.getMaxIterations();
		if (maxIterations < 0)
			maxIterations = (int)systemContext.getParamValue("iterations", Integer.class);
		double limit = systemContext.getParamValue("limit", Number.class).toDouble();
		double limitSq = limit*limit;

		List<Layer> layers = systemContext.getLayerConfiguration().getLayers();
		float[] sampleOffsets = new float[layers.get(layers.size()-1).getSampleCount()*2];
		for (int i = 0 ; i < samples ; i++){
			ComplexNumber offset = systemContext.getLayerConfiguration().getOffsetForSample(i);
			sampleOffsets[i*2] = (float)offset.realDouble();
			sampleOffsets[i*2+1] = (float)offset.imagDouble();
		}
		
		double[] params = new double[kernel.paramNames.length*3];
		
		for (int i = 0 ; i < kernel.paramNames.length ; i++){
			String name = kernel.paramNames[i];
			ParamSupplier supp = systemContext.getParamContainer().getClientParameter(name);
			
			if (supp == null){
				//search constant
				supp = kernel.constantSuppliers.get(name);
				//no constant found -> missing supplier
				if (supp == null)
					throw new IllegalStateException("Missing ParamSupplier "+name);
			}
			
			if (supp instanceof StaticParamSupplier){
				ComplexNumber val = (ComplexNumber)((StaticParamSupplier)supp).getObj();
				params[i*3] = (float) val.realDouble();
				params[i*3+1] = (float) val.imagDouble();
				params[i*3+2] = 0;
			}
			else if (supp instanceof CoordinateBasicShiftParamSupplier){
				CoordinateBasicShiftParamSupplier shiftSupp = (CoordinateBasicShiftParamSupplier) supp;
				params[i*3] = chunk.chunkPos.realDouble();
				params[i*3+1] = chunk.chunkPos.imagDouble();
				params[i*3+2] = systemContext.getPixelzoom().toDouble();
			}
			else
				throw new IllegalArgumentException("Unsupported ParamSupplier "+supp.getName()+": "+supp.getClass().getName());
		}
		
		for (int pixel = 0 ; pixel < chunk.getArrayLength() ; pixel++) {
			
			if (isCancelled())
				break;
			
			boolean pixelActive = layer.isActive(pixel);
			if (!pixelActive){
//				if (layer instanceof BreadthFirstUpsampleLayer)
//					pixel = ((BreadthFirstUpsampleLayer)layer).getEnabledPixels().nextEnabled(pixel)-1;
//				if (pixel == -2)
//					break;
				continue;
			}
			int currentSamples = chunk.getSampleCount(pixel);
			
			for (int sample = currentSamples ; sample < samples ; sample++) {
				
				calcPixel(chunk, kernel, upsample, maxIterations, limitSq, sampleOffsets, params, pixel,
						sample);
				
				
			}
		}
	}

	protected void calcPixel(AbstractArrayChunk chunk, EscapeTimeCpuKernelNew kernel, int upsample, int maxIterations,
			double limitSq, float[] sampleOffsets, double[] params, int pixel, int currentSamples) {
		
		float result = kernel.run(pixel, params, sampleOffsets, chunk.getChunkDimensions(), currentSamples, maxIterations, limitSq);
		
		chunk.addSample(pixel, result, upsample);
		
		if (result >= 0.0f && chunk.getValue(pixel, true) <= 0.0)
			sample_first_success(chunk, kernel, upsample, maxIterations, limitSq, sampleOffsets, params, pixel,
					currentSamples);
	}
	
	private EscapeTimeCpuKernelNew getCurrentKernel() {
			
		String new_expression = systemContext.getParamContainer().getClientParameter(BFOrbitCommon.PARAM_EXPRESSION).getGeneral(String.class);
		
		String inputVarName = null;
		if (new_expression.contains("X")) inputVarName = "X";
		if (new_expression.contains("x")) inputVarName = "x";
		if (new_expression.contains("Z")) inputVarName = "Z";
		if (new_expression.contains("z")) inputVarName = "z";
		
		ComputeExpressionBuilder expressionBuilder = new ComputeExpressionBuilder(new_expression, inputVarName, systemContext.getParamContainer().getClientParameters());
		ComputeExpression expression = expressionBuilder.getComputeExpression();
		
		List<Layer> layers = systemContext.getLayerConfiguration().getLayers();
		
		ComputeKernelParameters newKernelParameters = new ComputeKernelParameters(expression, layers, 0, 64);
		
		ComputeKernelParameters kernelParameters = tl_kernelParameters.get();
		if (kernelParameters == null || !kernelParameters.isCompartible(newKernelParameters, systemContext.getParamContainer())){
			tl_kernelParameters.set(newKernelParameters);
			EscapeTimeCpuKernelNew newKernel = (EscapeTimeCpuKernelNew) kernel_factory.createKernelCpu(EscapeTimeCpuKernelNew.class, newKernelParameters);
			tl_kernel.set(newKernel);
		}
		return tl_kernel.get();
	}
	
	public double[][] getTraces(){
		return getCurrentKernel().getTraces();
	}
	
	
	public void setTraceCount(int traceCount){
		getCurrentKernel().setTraces(traceCount);
	}
	
	private void sample_first_success(AbstractArrayChunk chunk, EscapeTimeCpuKernelNew kernel, int upsample, int maxIterations,
			double limitSq, float[] sampleOffsets, double[] params, int pixel, int currentSamples) {
		final int chunkDimensions = chunk.getChunkDimensions();
		final int chunkArrayLength = chunk.getArrayLength();
		int thisX = pixel / chunkDimensions;
		int thisY = pixel % chunkDimensions;
		//disable culling of surrounding pixels
		for (int dx = -1 ; dx <= 1 ; dx++) {
			for (int dy = -1 ; dy <= 1 ; dy++) {
				
				if (dx == 0 && dy == 0)
					continue;
				
				int x = thisX + dx*upsample;
				int y = thisY + dy*upsample;
				
				//determine if in neighbour chunk
				boolean local = true;
				if (x < 0){
					int low = thisY - thisY % (upsample);
					int high = low+upsample-1;
					local = false;
					if (y > 0 && y < chunkDimensions)
						chunk.getBorderData(BorderAlignment.LEFT).set(true, low, high);
				} else if (x >= chunkDimensions){
					int low = thisY - thisY % (upsample);
					int high = low+upsample-1;
					local = false;
					if (y > 0 && y < chunkDimensions)
						chunk.getBorderData(BorderAlignment.RIGHT).set(true, low, high);
				}
				
				if (y < 0){
					int low = thisX - thisX % (upsample);
					int high = low+upsample-1;
					local = false;
					if (x > 0 && x < chunkDimensions)
						chunk.getBorderData(BorderAlignment.UP).set(true, low, high);
				} else if (y >= chunkDimensions){
					int low = thisX - thisX % (upsample);
					int high = low+upsample-1;
					local = false;
					if (x > 0 && x < chunkDimensions)
						chunk.getBorderData(BorderAlignment.DOWN).set(true, low, high);
				}
				
				//local -> add to redo queue
				if (local){
					int pixel2 = x*chunkDimensions + y;
					if (chunk.getValue(pixel2, true) == AbstractArrayChunk.FLAG_CULL) {
						chunk.setCullFlags(pixel2, 1, false);
						calcPixel(chunk, getCurrentKernel(), upsample, maxIterations, limitSq, sampleOffsets, params, pixel2, currentSamples);
//						if (phase == CalculatePhase.PHASE_REDO || pixel > pixel2) {
//							redo.add(pixel2);
//						}
					}
				}
			}
		}
	}

}

enum CalculatePhase {
	PHASE_MAINLOOP, PHASE_REDO;
}
