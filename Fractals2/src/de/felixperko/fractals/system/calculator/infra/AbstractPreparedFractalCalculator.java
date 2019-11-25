package de.felixperko.fractals.system.calculator.infra;

import java.util.LinkedList;
import java.util.Queue;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.task.Layer;

public abstract class AbstractPreparedFractalCalculator extends AbstractFractalsCalculator{
	
	private static final long serialVersionUID = 8917363332981274755L;
	
	final static double LOG_2 = Math.log(2);
	
	public AbstractPreparedFractalCalculator() {
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
	
	ParamSupplier p_current;
	ParamSupplier p_pow;
	ParamSupplier p_c;
	
	IStats taskStats;
	
	@Override
	public void calculate(AbstractArrayChunk chunk, IStats taskStats) {
		
		this.chunk = chunk;
		this.taskStats = taskStats;
		
		limit = systemContext.getParamValue("limit", Double.class);
		p_current = systemContext.getParameters().get("start");
		p_pow = systemContext.getParameters().get("pow");
		p_c = systemContext.getParameters().get("c");
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();

		upsample = layer.getUpsample();
		int samples = layer.getSampleCount();
		maxIterations = layer.getMaxIterations();
		maxIterationsGlobal = systemContext.getParamValue("iterations", Integer.class);
		storeEndResults = maxIterations != -1 && maxIterations < maxIterationsGlobal;
		if (maxIterations == -1) {
			maxIterations = maxIterationsGlobal;
		}

		int chunkSize = chunk.getChunkDimensions();
		int pixelCount = chunk.getArrayLength();
		
		phase = CalculatePhase.PHASE_MAINLOOP;
		
		
		loop : 
		for (int pixel = 0 ; pixel < pixelCount ; pixel++) {
			if (!layer.isActive(pixel)){
				if (layer instanceof BreadthFirstUpsampleLayer)
					pixel = ((BreadthFirstUpsampleLayer)layer).getEnabledBitSet().nextSetBit(pixel)-1;
				if (pixel == -2)
					break;
				continue;
			}
			
			boolean cull = true;
			if (chunk.getValue(pixel, true) == AbstractArrayChunk.FLAG_CULL) {
				int x = pixel / chunkSize;
				int y = pixel % chunkSize;
				if (x-upsample < 0){
					if (chunk.getNeighbourBorderData(BorderAlignment.LEFT).isSet(y))
						cull = false;
				} else if (x+upsample >= chunkSize){
					if (chunk.getNeighbourBorderData(BorderAlignment.RIGHT).isSet(y))
						cull = false;
				}
				if (y-upsample < 0){
					if (chunk.getNeighbourBorderData(BorderAlignment.UP).isSet(x))
						cull = false;
				} else if (y+upsample >= chunkSize){
					if (chunk.getNeighbourBorderData(BorderAlignment.DOWN).isSet(x))
						cull = false;
				}
				if (cull)
					continue;
			}
			
			if (isCancelled())
				break loop;
			
			for (int sample = chunk.getSampleCount(pixel) ; sample < samples ; sample++){
				calculateSample(pixel, sample);
			}
			
			chunk.getCurrentTask().getStateInfo().setProgress((pixel+1.-redo.size())/pixelCount);
		}
		
		redoLoop:
		while (!redo.isEmpty()) {
			try {
				phase = CalculatePhase.PHASE_REDO;
				int pixel = redo.poll();
				for (int sample = chunk.getSampleCount(pixel) ; sample < samples ; sample++) {
					if (isCancelled()){
						redo.clear();
						break redoLoop;
					}
					calculateSample(pixel, sample);
				}
				chunk.getCurrentTask().getStateInfo().setProgress((pixelCount-redo.size())/pixelCount);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void calculateSample(int pixel, int sample) {
		double res = -1;
		ComplexNumber<?, ?> current = ((ComplexNumber)p_current.get(systemContext, chunk.chunkPos, pixel, sample)).copy();
		ComplexNumber<?, ?> c = ((ComplexNumber)p_c.get(systemContext, chunk.chunkPos, pixel, sample));
		ComplexNumber<?, ?> pow = ((ComplexNumber)p_pow.get(systemContext, chunk.chunkPos, pixel, sample));
		double logPow = Math.log(pow.absDouble());

		int k = 0;
		
		if (sample == 0) {
			ComplexNumber storedPosition = chunk.getStoredPosition(pixel);
			if (storedPosition != null) {
				current = storedPosition;
				k = chunk.getStoredIterations(pixel);
				chunk.removeStoredPosition(pixel);
			}
		}
		
		for ( ; k < maxIterations ; k++) {
			executeKernel(current, pow, c);
			
			if (trace)
				for (TraceListener listener : traceListeners)
					listener.trace(current, pixel, sample, k);
			
			double abs = current.absSqDouble();
			if (abs > limit*limit) {
//							Math.log( Math.log(real*real+imag*imag)*0.5 / Math.log(2) ) / Math.log(pow)  )
				res = k + 1 - Math.log(Math.log(abs)*0.5/LOG_2)/logPow; //abs...
				//res = k;
				sample_success(pixel);
				break;
			}
		}
		if (res == -1 && sample == 0 && storeEndResults)
			chunk.storeCurrentState(pixel, current, k+1);
		else 
			taskStats.addSample(k+1, res);
		
		chunk.addSample(pixel, res, upsample);
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

	public abstract void executeKernel(ComplexNumber<?, ?> current, ComplexNumber<?, ?> exp, ComplexNumber<?, ?> c);
	
	enum CalculatePhase {
		PHASE_MAINLOOP, PHASE_REDO;
	}
}
