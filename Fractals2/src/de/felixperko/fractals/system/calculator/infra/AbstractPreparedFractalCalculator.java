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

public abstract class AbstractPreparedFractalCalculator extends AbstractFractalsCalculator{
	
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
//		if (chunk.getChunkX() == 5 && chunk.getChunkY() == 10)
//			System.out.println("test chunk");
		for (int pixel = 0 ; pixel < chunk.getArrayLength() ; pixel++) {
			for (int sample = 0 ; sample < samples ; sample++){
				double res = -1;
				ComplexNumber current = ((ComplexNumber) p_start.get(pixel,sample)).copy();
				ComplexNumber c = ((ComplexNumber) p_c.get(pixel,sample)).copy();
				ComplexNumber pow = ((ComplexNumber) p_pow.get(pixel,sample)).copy();
				for (int k = 0 ; k < it ; k++) {
					executeKernel(current, pow, c);
					double abs = current.absSqDouble();
					if (abs > limit*limit) {
						res = k; //abs...
						break;
					}
				}
				chunk.addSample(pixel, res);
			}
		}
	}
	
	public abstract void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c);
}
