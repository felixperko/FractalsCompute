package de.felixperko.fractals.system.calculator.infra;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.AbstractFractalsCalculator;
import de.felixperko.fractals.system.parameters.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public abstract class AbstractPreparedFractalCalculator extends AbstractFractalsCalculator{
	
	private static final long serialVersionUID = 8917363332981274755L;
	
	final static double LOG_2 = Math.log(2);
	
	//ParamSupplier[] params;
	
	public AbstractPreparedFractalCalculator() {
		super(AbstractPreparedFractalCalculator.class);
	}

	ParamSupplier p_start;
	ParamSupplier p_c;
	ParamSupplier p_pow;
	ParamSupplier p_iterations;
	ParamSupplier p_limit;
	ParamSupplier p_samples;
	
	@Override
	public void calculate(Chunk chunk) {
		Double limit = (Double) p_limit.get(0,0);
		Integer it = (Integer) p_iterations.get(0,0);
		int samples = (Integer) p_samples.get(0,0);
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();
//		if (chunk.getChunkX() == 5 && chunk.getChunkY() == 10)
//			System.out.println("test chunk");
		
		int pixelCount = chunk.getArrayLength();
		
		loop : 
		for (int pixel = 0 ; pixel < pixelCount ; pixel++) {
			if (!layer.isActive(pixel))
				continue;
			for (int sample = chunk.getSampleCount(pixel) ; sample < samples ; sample++){
				if (cancelled)
					break loop;
				double res = -1;
				ComplexNumber current = ((ComplexNumber) p_start.get(pixel,sample)).copy();
				ComplexNumber c = ((ComplexNumber) p_c.get(pixel,sample)).copy();
				ComplexNumber pow = ((ComplexNumber) p_pow.get(pixel,sample)).copy();
				double logPow = Math.log(pow.absDouble());
				for (int k = 0 ; k < it ; k++) {
					executeKernel(current, pow, c);
					double abs = current.absSqDouble();
					if (abs > limit*limit) {
//									Math.log( Math.log(real*real+imag*imag)*0.5 / Math.log(2) ) / Math.log(pow)  )
						res = k - Math.log(Math.log(current.absSqDouble())*0.5/LOG_2)/logPow; //abs...
						break;
					}
				}
				chunk.addSample(pixel, res);
			}
			chunk.getCurrentTask().getStateInfo().setProgress((pixel+1.)/pixelCount);
		}
	}
	
	public abstract void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c);
}
