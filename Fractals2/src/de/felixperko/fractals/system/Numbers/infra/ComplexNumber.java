package de.felixperko.fractals.system.Numbers.infra;

import de.felixperko.fractals.system.Numbers.DoubleNumber;

public interface ComplexNumber<T, N> extends Number<N>{
	public T abs();
	public double absDouble();
	public double absSqDouble();
	public void multNumber(T number);
	//void mult(DoubleNumber number);
	public ComplexNumber copy();
	public void toPositive();
	public void div(N otherComplex);
	public void sub(N otherComplex);
	public double realDouble();
	public double realImag();
}
