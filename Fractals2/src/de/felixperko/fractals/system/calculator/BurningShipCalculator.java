package de.felixperko.fractals.system.calculator;

import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;

public class BurningShipCalculator extends AbstractPreparedFractalCalculator {
	
	private static final long serialVersionUID = 8448302722452695680L;

	@Override
	public void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c) {
		current.toPositive();
		current.pow(exp);
		current.add(c);
	}
}
