package de.felixperko.fractals.system.calculator;

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

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.system.calculator.infra.AbstractFractalsCalculator;
import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.util.expressions.GPUExpressionBuilder;

public class MandelbrotGPUCalculator extends AbstractFractalsCalculator{
	
	private static final long serialVersionUID = 1290699650814977951L;
	
	final static double LOG_2 = Math.log(2);
	
	public MandelbrotGPUCalculator() {
		super(AbstractPreparedFractalCalculator.class);
	}
	
	int maxIterations;
	int maxIterationsGlobal;
	boolean storeEndResults;
	
	double limit;
	int upsample;
	AbstractArrayChunk chunk;
	
	Queue<Integer> redo = new LinkedList<Integer>();
	
	CalculatePhase phase = null;
	
	IStats taskStats;
	
	int arrFillCounter = 0;
	
	
	static ThreadLocal<GPUExpressionKernel> tl_kernel = new ThreadLocal<>();
	
	
	@Override
	public void calculate(AbstractArrayChunk chunk, IStats taskStats, CalculateFractalsThread thread) {
		
		this.chunk = chunk;
		this.taskStats = taskStats;
		
		limit = systemContext.getParamValue("limit", Double.class);
//		p_current = systemContext.getParameters().get("start");
//		p_pow = systemContext.getParameters().get("pow");
//		p_c = systemContext.getParameters().get("c");
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();

		upsample = layer.getUpsample();
		maxIterations = layer.getMaxIterations();
		maxIterationsGlobal = systemContext.getParamValue("iterations", Integer.class);
		storeEndResults = maxIterations != -1 && maxIterations < maxIterationsGlobal;
		if (maxIterations == -1) {
			maxIterations = maxIterationsGlobal;
		}

		int pixelCount = chunk.getArrayLength();
		
		List<Layer> layers = systemContext.getLayerConfiguration().getLayers();
		
		GPUExpressionKernel kernel = getCurrentKernel(pixelCount, layers);
		
		int samples = layer.getSampleCount();
		int chunkSize = chunk.getChunkDimensions();
		
		prepareKernel(chunk, kernel, chunkSize, samples, layers.get(layers.size()-1).getSampleCount());
		executeKernel(chunk, layer, samples, pixelCount, kernel);
		
	}
	
	private GPUExpressionKernel getCurrentKernel(int pixelCount, List<Layer> layers) {
		GPUExpressionKernel kernel = tl_kernel.get();
		
		String new_expression = systemContext.getParamContainer().getClientParameter("f(z)=").getGeneral(String.class);
		
		if (kernel == null || kernel.hasExpressionChanged(new_expression) || kernel.pixelItArr.length != pixelCount){
			
			if (kernel != null)
				kernel.dispose();
			
//			expressions.add(new GPUExpression(GPUExpression.EXPR_WRITE_COMPLEX, 0, 1, 6, 7));
//			expressions.add(new GPUExpression(GPUExpression.EXPR_SQUARE, 6, 7, 2, 3));
//			expressions.add(new GPUExpression(GPUExpression.EXPR_ADD, 6, 7, 4, 5));
//			expressions.add(new GPUExpression(GPUExpression.EXPR_WRITE_COMPLEX, 6, 7, 0, 1));
			
			
//			List<GPUInstruction> instructions = new ArrayList<>();
////			instructions.add(new GPUInstruction(GPUInstruction.EXPR_ABS, 7, 7, 7, 7));
////			instructions.add(new GPUInstruction(GPUInstruction.EXPR_POW, 6, 7, 8, 9));
//////			expressions.add(new GPUExpression(GPUExpression.EXPR_ABS, 0, 1, 0, 1));
//			instructions.add(new GPUInstruction(GPUInstruction.EXPR_ABS, 0, 1, 0, 1));
//			instructions.add(new GPUInstruction(GPUInstruction.EXPR_POW, 0, 1, 2, 3));
//			instructions.add(new GPUInstruction(GPUInstruction.EXPR_ADD, 0, 1, 4, 5));
////			instructions.add(new GPUInstruction(GPUInstruction.EXPR_ADD, 0, 1, 6, 7));
////			instructions.add(new GPUInstruction(GPUInstruction.EXPR_WRITE_COMPLEX, 0, 1, 6, 7));
//			
//			Map<ParamSupplier, Integer> supplierMapping = new HashMap<>();
//			ParamContainer helperContainer = new ParamContainer(paramSuppliers);
////			supplierMapping.put(helperContainer.getClientParameter("start"), 0);
//			supplierMapping.put(helperContainer.getClientParameter("pow"), 2);
//			supplierMapping.put(helperContainer.getClientParameter("c"), 4);
////			supplierMapping.put(helperContainer.getClientParameter("placeholder_prev"), 6);
////			supplierMapping.put(helperContainer.getClientParameter("placeholder_prev_pow"), 8);
//			GPUExpression expression = new GPUExpression(instructions, supplierMapping);
			
			
//			Map<String, ParamSupplier> suppliers = new HashMap<>();
//			for (ParamSupplier supp : paramSuppliers)
//				suppliers.put(supp.getName(), supp);
			String formula = systemContext.getParamContainer().getClientParameter("f(z)=").getGeneral(String.class);
//			String formula = "z^pow+c";
			
//			GPUExpression expression = FractalsPolynomParser.parsePolynom(formula, suppliers, systemContext.getNumberFactory()).getGpuInstructions();
			String inputVarName = null;
			if (new_expression.contains("X")) inputVarName = "X";
			if (new_expression.contains("x")) inputVarName = "x";
			if (new_expression.contains("Z")) inputVarName = "Z";
			if (new_expression.contains("z")) inputVarName = "z";
			
			GPUExpressionBuilder expressionBuilder = new GPUExpressionBuilder(new_expression, inputVarName, systemContext.getParamContainer().getClientParameters());
			GPUExpression expression = expressionBuilder.getGpuExpression();
			
			if (expression == null)
				throw new InvalidParameterException("The input expression can't be parsed");
//			expression.instructions.add(0, new GPUInstruction(GPUInstruction.EXPR_ABS, 1, 1, 1, 1));
			NumberFactory nf = systemContext.getNumberFactory();
//			GPUExpression expression = new GPUExpression(instructions, paramSuppliers);
			
			kernel = GPUExpressionKernel.createKernel(new GpuAsmExpressionKernelFactory(), expression, layers, pixelCount);
 			tl_kernel.set(kernel);
			
			CacheEnabler.setCachesEnabled(false);
//			CacheEnabler.setCachesEnabled(true);
		}
		return kernel;
	}
	
	private void prepareKernel(AbstractArrayChunk chunk, GPUExpressionKernel kernel, int chunkSize, int samples, int layerSampleCount) {
		kernel.optionsInt[0] = maxIterations;
		kernel.optionsInt[1] = chunkSize;
		kernel.optionsInt[2] = samples;
		kernel.optionsFloat[0] = (float)(double)systemContext.getParamValue("limit", Double.class);

		for (int i = 0 ; i < layerSampleCount ; i++){
			ComplexNumber offset = systemContext.getLayerConfiguration().getOffsetForSample(i);
			kernel.sampleOffsets[i*2] = (float)offset.realDouble();
			kernel.sampleOffsets[i*2+1] = (float)offset.imagDouble();
		}
		
		paramLoop:
			for (int i = 0 ; i < kernel.paramNames.length ; i++){
				String name = kernel.paramNames[i];
				ParamSupplier supp = systemContext.getParamContainer().getClientParameter(name);
//			for (int j = 0 ; j < paramSuppliers.size(); j++){
//				if (paramSuppliers.get(j).getName().equalsIgnoreCase(name)){
//					supp = paramSuppliers.get(j);
//					break;
//				}
//			}
				if (supp == null){
					//search constant
					for (int j = 0 ; j < kernel.constantNames.length ; j++){
						if (kernel.constantNames[j].equalsIgnoreCase(name)){
							kernel.params[i*3  ] = kernel.constants[j*2  ];
							kernel.params[i*3+1] = kernel.constants[j*2+1];
							kernel.params[i*3+2] = 0;
							continue paramLoop;
						}
					}
					//no constant found -> missing supplier
					throw new IllegalStateException("Missing ParamSupplier "+name);
				}
				if (supp instanceof StaticParamSupplier){
					ComplexNumber val = (ComplexNumber)((StaticParamSupplier)supp).getObj();
					kernel.params[i*3] = (float) val.realDouble();
					kernel.params[i*3+1] = (float) val.imagDouble();
					kernel.params[i*3+2] = 0;
				}
				else if (supp instanceof CoordinateBasicShiftParamSupplier){
					CoordinateBasicShiftParamSupplier shiftSupp = (CoordinateBasicShiftParamSupplier) supp;
					kernel.params[i*3] = chunk.chunkPos.realDouble();
					kernel.params[i*3+1] = chunk.chunkPos.imagDouble();
					kernel.params[i*3+2] = systemContext.getPixelzoom().toDouble();
				}
				else
					throw new IllegalArgumentException("Unsupported ParamSupplier "+supp.getName()+": "+supp.getClass().getName());
			}
	}

	private void executeKernel(AbstractArrayChunk chunk, Layer layer, int samples, int pixelCount,
			GPUExpressionKernel kernel) {
		phase = CalculatePhase.PHASE_MAINLOOP;
		
		//get max amount of samples that have to be completed for any pixel
		int currentMinSamples = Integer.MAX_VALUE;
		for (int sample = 0 ; sample < samples ; sample++) {
//			GPUSampleDataBuilder sampleDataBuilder = new GPUSampleDataBuilder(maxIterations);
			
			if (isCancelled())
				break;
			
			loop : 
				for (int pixel = 0 ; pixel < pixelCount ; pixel++) {
					
					int currentSamples = chunk.getSampleCount(pixel);
					if (currentSamples >= samples)
						continue;
					
					if (!layer.isActive(pixel)){
						if (layer instanceof BreadthFirstUpsampleLayer)
							pixel = ((BreadthFirstUpsampleLayer)layer).getEnabledPixels().nextEnabled(pixel)-1;
						if (pixel == -2)
							break;
						continue;
					}
					if (currentSamples < currentMinSamples)
						currentMinSamples = currentSamples;
					
					
				}
		}
		
		//execute Kernel and store results
		KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
		List<Device> devices = new ArrayList<>(preferences.getPreferredDevices(null));
		KernelManager.instance().setDefaultPreferredDevices(new LinkedHashSet<>(devices));
		for (int sample = currentMinSamples ; sample < samples ; sample++){
			boolean noChange = true;
			if (isCancelled())
				return;
			Range range = kernel.device.createRange((int) (Math.ceil(pixelCount/256d)*256), 256);
			kernel.setExplicit(false);
//				kernel.put(currentRealArr);
//				kernel.put(currentImagArr);
//				kernel.put(cRealArr);
//				kernel.put(cImagArr);
//				kernel.put(pixelItArr);
////				//TODO pow if changed
			
			for (int i = 0 ; i < pixelCount ; i++){
				int index = i;
				kernel.pixelSamples[i] = layer.isActive(i) ? chunk.getSampleCount(index) : samples;
				kernel.pixelWasSampled[i] = 0;
			}
			kernel.execute(range);
//				kernel.get(resultsArr);
//				kernel.dispose();
			
			for (int i = 0 ; i < pixelCount ; i++){
				int sucessfulSamples = kernel.pixelWasSampled[i];
				if (sucessfulSamples > 0){
					chunk.addSample(i, kernel.resultsArr[i], upsample);
					sample_success(i);
					noChange = false;
				}
			}
			for (int i = 0 ; i < kernel.resultsArr.length ; i++)
				kernel.resultsArr[i] = 0;
			if (noChange)
				break;
		}
//			}
		
		arrFillCounter = 0;
	}
	private void sample_success(int pixel) {
//		if (chunk.getValue(pixel, true) <= 0)
			sample_first_success(pixel);
			
	}
	
	private void sample_first_success(int pixel) {
		final int chunkDimensions = chunk.getChunkDimensions();
		final int upsample = chunk.getUpsample();
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
						if (phase == CalculatePhase.PHASE_REDO || pixel > pixel2) {
							redo.add(pixel2);
						}
					}
				}
			}
		}
	}
	
	enum CalculatePhase {
		PHASE_MAINLOOP, PHASE_REDO;
	}
}
