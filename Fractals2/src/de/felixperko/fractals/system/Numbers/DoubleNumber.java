package de.felixperko.fractals.system.Numbers;
import de.felixperko.fractals.system.Numbers.infra.Number;

public class DoubleNumber implements Number<DoubleNumber> {

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
	public void mult(DoubleNumber other) {
		value *= other.value;
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
}
