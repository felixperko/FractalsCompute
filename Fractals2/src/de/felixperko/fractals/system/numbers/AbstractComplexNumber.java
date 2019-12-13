package de.felixperko.fractals.system.numbers;

import de.felixperko.fractals.util.serialization.jackson.JsonValueWrapper;

public abstract class AbstractComplexNumber<NUMBER extends Number<NUMBER>, COMPLEXNUMBER extends ComplexNumber<NUMBER,COMPLEXNUMBER>> implements ComplexNumber<NUMBER, COMPLEXNUMBER> {

	private static final long serialVersionUID = 1354288026927154373L;
	
	public AbstractComplexNumber() {
		
	}
	
	public AbstractComplexNumber(NUMBER real, NUMBER imag) {
		
	}

	@Override
	public JsonValueWrapper toWrappedValue() {
		return new JsonValueWrapper(this);
	}
	
}
