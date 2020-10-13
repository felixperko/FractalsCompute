package de.felixperko.fractals.system.calculator.infra;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public abstract class NewtonFractalCalculator extends AbstractFractalsCalculator{

	private static final long serialVersionUID = -6099194301035200237L;
	
//	ParamSupplier p_iterations;
//	ParamSupplier p_samples;
//	ParamSupplier p_start;
//	ParamSupplier p_limit;
//	ParamSupplier p_c;
	
	protected DoubleComplexNumber[] roots;
	
	int maxIterations;
	int maxIterationsGlobal;
	boolean storeEndResults;
	
	IStats taskStats;
	
	public NewtonFractalCalculator() {
	}

	@Override
	public void calculate(AbstractArrayChunk chunk, IStats taskStats, CalculateFractalsThread thread) {
		this.taskStats = taskStats;
		setRoots();
		double limit = (Double) systemContext.getParamValue("limit", Double.class);
		int it = (Integer) systemContext.getParamValue("iterations", Integer.class);
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();
		
		maxIterations = layer.getMaxIterations();
		maxIterationsGlobal = systemContext.getParamValue("iterations", Integer.class);
		storeEndResults = maxIterations != -1 && maxIterations < maxIterationsGlobal;
		if (maxIterations == -1) {
			maxIterations = maxIterationsGlobal;
		}
		
		int samples = layer.getSampleCount();
		int upsample = (layer instanceof BreadthFirstUpsampleLayer) ? ((BreadthFirstUpsampleLayer)layer).getUpsample()/2 : 0;
		loop : 
		for (int pixel = 0 ; pixel < chunk.getArrayLength() ; pixel++) {
			if (!layer.isActive(pixel))
				continue;
			for (int sample = 0 ; sample < samples ; sample++){
				if (cancelled)
					break loop;
				ComplexNumber current = systemContext.getParamValue("start", ComplexNumber.class, chunk.chunkPos, pixel, sample).copy();
//				boolean test = current.realDouble() == -2. && current.imagDouble() == -2.;
				ComplexNumber c = systemContext.getParamValue("c", ComplexNumber.class, chunk.chunkPos, pixel, sample);
				ComplexNumber copy1;
				ComplexNumber copy2;

				int j = 0;
				int i = 0;
				iterationLoop:
				for ( ; j < it ; j++) {
					if (sample == 0) {
						ComplexNumber storedPosition = chunk.getStoredPosition(pixel);
						if (storedPosition != null) {
							current = storedPosition;
							j = chunk.getStoredIterations(pixel);
							chunk.removeStoredPosition(pixel);
						}
					}
					copy1 = current.copy();
					copy2 = current.copy();
					executeFunctionKernel(copy1);
					executeDerivativeKernel(copy2);
					copy1.div(copy2);
					current.sub(copy1);
					
					if (trace)
						for (TraceListener listener : traceListeners)
							listener.trace(current, pixel, sample, j);
					
					for (i = 0 ; i < roots.length ; i++) {
						DoubleComplexNumber root = roots[i];
						if (Math.abs(current.realDouble()-root.realDouble()) < limit && Math.abs(current.imagDouble()-root.imagDouble()) < limit) {//TODO arbitrary precision
//							chunk.addSample(pixel, getRootValue(i, j, it), upsample);
							break iterationLoop;
						}
					}
					i = -1;
				}
				
				if (i == -1 && sample == 0 && storeEndResults)
					chunk.storeCurrentState(pixel, current, j+1);
				else {
					taskStats.addSample(j+1, i+1);
					thread.addIterations(j);
				}
				
				double sampleValue = i == -1 ? -1 : getRootValue(i, j, it);
				chunk.addSample(pixel, sampleValue, upsample);
			}
			chunk.getCurrentTask().getStateInfo().setProgress((pixel/(double)chunk.getArrayLength()));
		}
	}
	
	public abstract void setRoots();
	
	public double getRootValue(int root, int iterations, int max_iterations){
		return 1 + root + iterations/(double)maxIterations;
	}

	public abstract void executeFunctionKernel(ComplexNumber z_copy);

	public abstract void executeDerivativeKernel(ComplexNumber z_copy);

}
