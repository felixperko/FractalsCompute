package de.felixperko.fractals.system.calculator;

import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.NewtonFractalCalculator;

public class NewtonThridPowerMinusOneCalculator extends NewtonFractalCalculator {

	private static final long serialVersionUID = -2057843694568405589L;

	@Override
	public void setRoots() {
		roots = new DoubleComplexNumber[3];
		double sqrt3 = Math.sqrt(3);
		roots[0] = new DoubleComplexNumber(1, 0);
		roots[1] = new DoubleComplexNumber(-.5, sqrt3/2);
		roots[2] = new DoubleComplexNumber(-.5, -sqrt3/2);
	}

	@Override
	public double getRootValue(int root) {
		switch (root) {
		case 0:
			return 10;
		case 1:
			return 20;
		case 2:
			return 30;
		default:
			return -1;
		}
	}
	
	DoubleComplexNumber pow1 = new DoubleComplexNumber(3, 0);
	DoubleComplexNumber add1 = new DoubleComplexNumber(-1, 0);
	DoubleComplexNumber pow2 = new DoubleComplexNumber(2, 0);
	DoubleComplexNumber mult2 = new DoubleComplexNumber(3, 0);

	@Override
	public void executeFunctionKernel(ComplexNumber z_copy) {
		if (!(z_copy instanceof DoubleComplexNumber))
			throw new IllegalArgumentException("only supports DoubleComplexNumbers for now.");
		DoubleComplexNumber z = (DoubleComplexNumber)z_copy;
		z.pow(pow1);
		z.add(add1);
	}

	@Override
	public void executeDerivativeKernel(ComplexNumber z_copy) {
		if (!(z_copy instanceof DoubleComplexNumber))
			throw new IllegalArgumentException("only supports DoubleComplexNumbers for now.");
		DoubleComplexNumber z = (DoubleComplexNumber)z_copy;
		z.pow(pow2);
		z.mult(mult2);
	}

}
