package de.felixperko.fractals.system.numbers.impl;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.parameters.suppliers.Copyable;
import de.felixperko.fractals.util.serialization.jackson.JsonValueWrapper;

public class DoubleNumber implements Number<DoubleNumber>, Copyable<DoubleNumber> {

	private static final long serialVersionUID = -7389627450068792267L;
	
	double value;
	
	public DoubleNumber() {
		value = 0;
	}
	
	public DoubleNumber(String valueString) {
		value = Double.parseDouble(valueString);
	}
	
	public DoubleNumber(double valueDouble) {
		value = valueDouble;
	}
	
	@Override
	public void add(DoubleNumber other) {
		value += other.value;
	}
	
	@Override
	public void sub(DoubleNumber other) {
		value -= other.value;
	}
	
	@Override
	public void mult(DoubleNumber other) {
		value *= other.value;
	}
	
	@Override
	public void div(DoubleNumber other) {
		value /= other.value;
	}

	@Override
	public void square() {
		value *= value;
	}
	
	@Override
	public void pow(DoubleNumber exp) {
		if (exp.value == 2)
			square();
		else
			value = Math.pow(value, exp.value);
	}

	@Override
	public double toDouble() {
		return value;
	}
	
	@Override
	public String toString() {
		return ""+value;
	}

	
	@Override
	public DoubleNumber copy() {
		return new DoubleNumber(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Number))
			return false;
		return ((Number)obj).toDouble() == value;
	}

	@Override
	public JsonValueWrapper toWrappedValue() {
		return new JsonValueWrapper(this);
	}
}
