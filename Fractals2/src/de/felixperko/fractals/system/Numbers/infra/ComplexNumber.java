package de.felixperko.fractals.system.Numbers.infra;

import de.felixperko.fractals.system.Numbers.DoubleNumber;

public interface ComplexNumber<T extends Number, N extends ComplexNumber> extends Number<N>{
	public T abs();
	public double absDouble();
	public double absSqDouble();
	public void multNumber(T number);
	public void divNumber(T number);
	//void mult(DoubleNumber number);
	public N copy();
	public void toPositive();
	public void add(N otherComplex);
	public void sub(N otherComplex);
	public void div(N otherComplex);
	public double realDouble();
	public double imagDouble();
	public T getReal();
	public T getImag();
}
