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
	
	@Override
	public void calculate(Chunk chunk) {
		Double limit = (Double) p_limit.get(0);
		Integer it = (Integer) p_iterations.get(0);
//		if (chunk.getChunkX() == 5 && chunk.getChunkY() == 10)
//			System.out.println("test chunk");
		for (int pixel = 0 ; pixel < chunk.getArrayLength() ; pixel++) {
			ComplexNumber current = ((ComplexNumber) p_start.get(pixel)).copy();
			ComplexNumber c = (ComplexNumber) p_c.get(pixel);
			ComplexNumber pow = (ComplexNumber) p_pow.get(pixel);
			double res = -1;
			for (int j = 0 ; j < it ; j++) {
				executeKernel(current, pow, c);
				double abs = current.absSqDouble();
				if (abs > limit*limit) {
					res = j; //abs...
					break;
				}
			}
			chunk.addSample(pixel, res);
		}
	}
	
	public abstract void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c);
}
