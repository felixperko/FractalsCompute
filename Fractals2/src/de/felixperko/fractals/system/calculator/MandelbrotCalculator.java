package de.felixperko.fractals.system.calculator;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public class MandelbrotCalculator extends AbstractPreparedFractalCalculator {
	
	@Override
	public void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c) {
		current.pow(exp);
		current.add(c);
	}

}
