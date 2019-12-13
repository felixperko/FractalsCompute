package de.felixperko.fractals.system.numbers;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonValue;

import de.felixperko.fractals.util.serialization.jackson.JsonValueWrapper;

public interface ComplexNumber<NUMBER extends Number<NUMBER>, COMPLEX extends ComplexNumber<NUMBER, COMPLEX>> extends Serializable{
	public NUMBER abs();
	public double absDouble();
	public double absSqDouble();
	public void multNumber(NUMBER number);
	public void multValues(COMPLEX otherComplex);
	public void divNumber(NUMBER number);
	public COMPLEX copy();
	public void toPositive();
	public void reciprocal();
	public void complexConjugate();
	public void add(COMPLEX otherComplex);
	public void mult(COMPLEX otherComplex);
	public void sub(COMPLEX otherComplex);
	public void div(COMPLEX otherComplex);
	public void pow(COMPLEX otherComplex);
	public double realDouble();
	public double imagDouble();
	public NUMBER getReal();
	public NUMBER getImag();
	public void square();
	@JsonValue
	public JsonValueWrapper toWrappedValue();
}
