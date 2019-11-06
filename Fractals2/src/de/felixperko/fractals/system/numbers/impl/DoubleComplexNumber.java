package de.felixperko.fractals.system.numbers.impl;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.system.numbers.AbstractComplexNumber;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.Copyable;
import de.felixperko.fractals.util.NumberUtil;

public class DoubleComplexNumber extends AbstractComplexNumber<DoubleNumber, DoubleComplexNumber> implements Copyable<DoubleComplexNumber>{
	
	private static final long serialVersionUID = -6625226042922366712L;
	
	public static void main(String[] args) {
		DoubleComplexNumber val = new DoubleComplexNumber(1.1, 0);
		DoubleComplexNumber pow = new DoubleComplexNumber(-2, 0);
		System.out.println(val.toString()+" ^ ("+pow.toString()+")");
		int steps = 1 << 2;
		System.out.println("steps: "+steps);
		long t1 = System.nanoTime();
		for (int i = 0 ; i < steps ; i++)
			val.pow(pow);
		long t2 = System.nanoTime();
		System.out.println(val.toString());
		System.out.println("time: "+NumberUtil.NS_TO_MS*(t2-t1)+" ms");
	}
	
	double real, imag;
	
	public DoubleComplexNumber() {
		super();
		real = 0;
		imag = 0;
	}
	
	public DoubleComplexNumber(DoubleNumber real, DoubleNumber imag) {
		super(real, imag);
		this.real = real.value;
		this.imag = imag.value;
	}
	
	public DoubleComplexNumber(String real, String imag) {
		this.real = Double.parseDouble(real);
		this.imag = Double.parseDouble(imag);
	}
	
	public DoubleComplexNumber(double real, double imag) {
		this.real = real;
		this.imag = imag;
	}

	@Override
	public void add(DoubleComplexNumber other) {
		real += other.real;
		imag += other.imag;
	}

	@Override
	public void mult(DoubleComplexNumber other) {
		double temp = real*other.real - imag*other.imag;
		imag = real*other.imag + imag*other.real;
		real = temp;
	}
	
	@Override
	public void multNumber(DoubleNumber number) {
		real *= number.value;
		imag *= number.value;
	}
	
	@Override
	public void multValues(DoubleComplexNumber otherComplex) {
		real *= otherComplex.real;
		imag *= otherComplex.imag;
	}
	
	@Override
	public void divNumber(DoubleNumber number) {
		real /= number.value;
		imag /= number.value;
	}

	@Override
	public void pow(DoubleComplexNumber other) {
		if (other.real == 0 && other.imag == 0){
			real = 0;
			imag = 0;
			return;
		}
		if (other.imag == 0) {
			if (other.real == 2) //just squared
				square();
			else if (other.real%1 == 0) {
				double powLeft = other.real;
				boolean negativePower = powLeft < 0;
				if (negativePower) {
					powLeft = -powLeft;
				}
				
				int squarePow = 2;
				List<DoubleComplexNumber> pow2Buffer = new ArrayList<>();
				
				//square as long as possible
				for ( ; squarePow <= powLeft ; ) {
					pow2Buffer.add(copy());
					square();
					squarePow <<= 1;
				}
				
				powLeft -= squarePow >> 1;
				if (powLeft == 0) {
					if (negativePower) {
						real = real == 0 ? 0 : 1/real;
						imag = imag == 0 ? 0 : 1/imag;
					}
					return;
				}
				
				//add intermediate results
				int powStep = 1 << (pow2Buffer.size()-1);
				for (int i = pow2Buffer.size()-1 ; i >= 0 ; i--) {
					if (powLeft >= powStep) {
						mult(pow2Buffer.get(i));
						powLeft -= powStep;
						if (powLeft == 0) {
							if (negativePower) {
								real = real == 0 ? 0 : 1/real;
								imag = imag == 0 ? 0 : 1/imag;
							}
							return;
						}
					}
					powStep >>= 1;
				}
			}
			else {
				double abs = absDouble();
				double logReal = Math.log(abs);
				double logImag = Math.atan2(imag, real);
				//mult
				logReal = logReal*other.real;
				logImag = logImag*other.real;
				//exp
				double expReal = Math.exp(logReal);
				real = expReal * Math.cos(logImag);
				imag = expReal * Math.sin(logImag);
			}
		}
		else { //complex power
			//log().multiply(x).exp()
			//log
			double abs = absDouble();
			double logReal = 0;
			if (abs != 0)
				logReal = Math.log(abs);
			double logImag = Math.atan2(imag, real);
			//mult
			double temp = logReal*other.real - logImag*other.imag;
			logImag = logReal*other.imag + logImag*other.real;
			logReal = temp;
			//exp
			double expReal = Math.exp(logReal);
			real = expReal * Math.cos(logImag);
			imag = expReal * Math.sin(logImag);
		}
	}
	
	@Override
	public DoubleNumber abs() {
		return new DoubleNumber(real*real + imag*imag);
	}
	
	@Override
	public double absDouble() {
		return Math.sqrt(real*real + imag*imag);
	}

	@Override
	public double absSqDouble() {
		return real*real + imag*imag;
	}
	
	@Override
	public void square() {
		double temp = real*real - imag*imag;
		imag = real*imag*2;
		real = temp;
	}
	
	@Override
	public String toString() {
		return real+","+imag;
	}

	@Override
	public DoubleComplexNumber copy() {
		return new DoubleComplexNumber(real, imag);
	}
	
	@Override
	public void toPositive() {
		if (real < 0)
			real = -real;
		if (imag < 0)
			imag = -imag;
	}

	@Override
	public void div(DoubleComplexNumber otherComplex) {
		DoubleComplexNumber n = (DoubleComplexNumber) otherComplex.copy();
		n.reciprocal();
		mult(n);
	}
	
	public void reciprocal() {
		double scale = real*real + imag*imag;
		real /= scale;
		imag /= -scale;
	}

	@Override
	public void sub(DoubleComplexNumber otherComplex) {
		real -= otherComplex.real;
		imag -= otherComplex.imag;
	}

	@Override
	public double realDouble() {
		return real;
	}

	@Override
	public double imagDouble() {
		return imag;
	}
	
	@Override
	public DoubleNumber getReal() {
		return new DoubleNumber(real);
	}

	@Override
	public DoubleNumber getImag() {
		return new DoubleNumber(imag);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof DoubleComplexNumber)) {
			if (obj instanceof ComplexNumber)
				return getReal().equals(((ComplexNumber) obj).getReal()) && getImag().equals(((ComplexNumber) obj).getImag());
			return false;
		}
		
		DoubleComplexNumber other = (DoubleComplexNumber)obj;
		return real == other.real && imag == other.imag;
	}

	
	@Override
	public void complexConjugate() {
		imag = -imag;
	}
}
