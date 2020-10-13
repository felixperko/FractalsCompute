package de.felixperko.fractals.system.calculator;

import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;

@Deprecated
public class FibonacciPowCalculator extends AbstractPreparedFractalCalculator{

	private static final long serialVersionUID = 5404872334354603997L;
	
	ComplexNumber c_last = null;
	ComplexNumber exp_last = null;
	ComplexNumber temp_pow;
	ComplexNumber temp;
	DoubleComplexNumber complex00 = new DoubleComplexNumber(0,0);
	DoubleComplexNumber complex10 = new DoubleComplexNumber(1,0);
	
	@Override
	public void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c) {
		//TODO replace DoubleComplexNumber instantiation with NumberFactory
		if (c_last == null || !c.equals(c_last)) {
			exp_last = new DoubleComplexNumber(0, 0);
			c_last = c;
		} else {
			temp_pow = exp.copy();
			exp.add(exp_last);
			exp_last = temp_pow;
		}
		
		temp = current.copy();
		temp.reciprocal();
//		temp.complexConjugate();
		temp.add(complex10);
		temp.pow(exp);
		
		current.add(temp);
		current.add(c);
	}

}
