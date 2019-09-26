package de.felixperko.fractals.system.calculator;

import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;

public class TricornCalculator extends AbstractPreparedFractalCalculator {
	
	@Override
	public void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c) {
		current.pow(exp);
		current.add(c);
		current.complexConjugate();
	}

}
