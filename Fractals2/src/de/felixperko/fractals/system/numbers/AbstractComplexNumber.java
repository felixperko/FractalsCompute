package de.felixperko.fractals.system.numbers;

public abstract class AbstractComplexNumber<T extends Number<T>, N extends ComplexNumber<T,N>> implements ComplexNumber<T, N> {

	private static final long serialVersionUID = 1354288026927154373L;
	
	public AbstractComplexNumber() {
		
	}
	
	public AbstractComplexNumber(T real, T imag) {
		
	}
	
}
