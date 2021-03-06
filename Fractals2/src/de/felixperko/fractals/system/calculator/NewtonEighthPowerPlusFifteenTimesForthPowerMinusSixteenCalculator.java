package de.felixperko.fractals.system.calculator;

import de.felixperko.fractals.system.calculator.infra.NewtonFractalCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;

public class NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator extends NewtonFractalCalculator{

	private static final long serialVersionUID = 7374253894015410365L;

	public NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator() {
	}
	
	double sqrt2 = Math.sqrt(2);

	@Override
	public void setRoots() {
		roots = new DoubleComplexNumber[8];
		roots[0] = new DoubleComplexNumber(1,0);
		roots[1] = new DoubleComplexNumber(-1,0);
		roots[2] = new DoubleComplexNumber(0,1);
		roots[3] = new DoubleComplexNumber(0,-1);
		roots[4] = new DoubleComplexNumber(sqrt2,sqrt2);
		roots[5] = new DoubleComplexNumber(-sqrt2,-sqrt2);
		roots[6] = new DoubleComplexNumber(-sqrt2,sqrt2);
		roots[7] = new DoubleComplexNumber(sqrt2,-sqrt2);
	}

//	@Override
//	public double getRootValue(int root) {
//		//return Math.pow(root+1, 2);
//	}
	
	DoubleComplexNumber pow11 = new DoubleComplexNumber(8, 0);
	DoubleComplexNumber pow12 = new DoubleComplexNumber(4, 0);
	DoubleComplexNumber pow21 = new DoubleComplexNumber(7, 0);
	DoubleComplexNumber pow22 = new DoubleComplexNumber(3, 0);
	DoubleComplexNumber mult12 = new DoubleComplexNumber(15, 0);
	DoubleComplexNumber mult21 = new DoubleComplexNumber(8, 0);
	DoubleComplexNumber mult22 = new DoubleComplexNumber(60, 0);
	DoubleComplexNumber add1 = new DoubleComplexNumber(-16, 0);

	@Override
	public void executeFunctionKernel(ComplexNumber z_copy) {
		if (!(z_copy instanceof DoubleComplexNumber))
			throw new IllegalArgumentException("only supports DoubleComplexNumbers for now.");
		ComplexNumber z = z_copy;
		ComplexNumber z2 = z.copy();
		//x^8 + 15*x^4 - 16
		z.pow(pow11);
		z2.pow(pow12);
		z2.mult(mult12);
		z.add(z2);
		z.add(add1);
	}

	@Override
	public void executeDerivativeKernel(ComplexNumber z_copy) {
		if (!(z_copy instanceof DoubleComplexNumber))
			throw new IllegalArgumentException("only supports DoubleComplexNumbers for now.");
		DoubleComplexNumber z = (DoubleComplexNumber)z_copy;
		DoubleComplexNumber z2 = (DoubleComplexNumber)z.copy();
		//8x^7 + 60*x^3
		z.pow(pow21);
		z.mult(mult21);
		z2.pow(pow22);
		z2.mult(mult22);
		z.add(z2);
	}

}
