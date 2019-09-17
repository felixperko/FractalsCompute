package de.felixperko.fractals.system.Numbers.infra;

import java.io.Serializable;

public interface ComplexNumber<T extends Number<T>, N extends ComplexNumber<T, N>> extends Serializable{
	public T abs();
	public double absDouble();
	public double absSqDouble();
	public void multNumber(T number);
	public void multValues(N otherComplex);
	public void divNumber(T number);
	//void mult(DoubleNumber number);
	public N copy();
	public void toPositive();
	public void complexConjugate();
	public void add(N otherComplex);
	public void mult(N otherComplex);
	public void sub(N otherComplex);
	public void div(N otherComplex);
	public void pow(N otherComplex);
	public double realDouble();
	public double imagDouble();
	public T getReal();
	public T getImag();
	public void square();
}
