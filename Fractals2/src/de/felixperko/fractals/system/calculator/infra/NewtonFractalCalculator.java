package de.felixperko.fractals.system.calculator.infra;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public abstract class NewtonFractalCalculator extends AbstractFractalsCalculator{

	ParamSupplier p_iterations;
	ParamSupplier p_start;
	ParamSupplier p_limit;
	ParamSupplier p_c;
	
	protected DoubleComplexNumber[] roots;
	
	public NewtonFractalCalculator() {
		super(NewtonFractalCalculator.class);
	}

	@Override
	public void calculate(Chunk chunk) {
		setRoots();
		double limit = (Double) p_limit.get(0); //TODO arbitrary precision
		int it = (Integer) p_iterations.get(0);
		for (int pixel = 0 ; pixel < chunk.getArrayLength() ; pixel++) {
			ComplexNumber current = ((ComplexNumber) p_start.get(pixel)).copy();
			boolean test = current.realDouble() == -2. && current.realImag() == -2.;
			ComplexNumber c = (ComplexNumber) p_c.get(pixel);
			ComplexNumber copy1;
			ComplexNumber copy2;
			double res = -1;
			for (int j = 0 ; j < it ; j++) {
				copy1 = current.copy();
				copy2 = current.copy();
				executeFunctionKernel(copy1);
				executeDerivativeKernel(copy2);
				copy1.div(copy2);
				current.sub(copy1);
				if (test)
					System.out.println("("+current.toString()+")");
				
				for (int i = 0 ; i < roots.length ; i++) {
					DoubleComplexNumber root = roots[i];
					if (Math.abs(current.realDouble()-root.realDouble()) < limit && Math.abs(current.realImag()-root.realImag()) < limit) {
						chunk.addSample(pixel, getRootValue(i));
						break;
					}
				}
			}
		}
	}
	
	public abstract void setRoots();
	
	public abstract double getRootValue(int root);

	public abstract void executeFunctionKernel(ComplexNumber z_copy);

	public abstract void executeDerivativeKernel(ComplexNumber z_copy);

}
