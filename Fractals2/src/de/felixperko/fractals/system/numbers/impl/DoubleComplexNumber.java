package de.felixperko.fractals.system.numbers.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonValue;

import de.felixperko.fractals.system.numbers.AbstractComplexNumber;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.Copyable;
import de.felixperko.fractals.util.NumberUtil;

public class DoubleComplexNumber extends AbstractComplexNumber<DoubleNumber, DoubleComplexNumber> implements Copyable<DoubleComplexNumber>{
	
	private static final long serialVersionUID = -6625226042922366712L;
	private static final Logger LOG = LoggerFactory.getLogger(DoubleComplexNumber.class);
	
	public static void main(String[] args) {
		DoubleComplexNumber val = new DoubleComplexNumber(1.1, 0);
		DoubleComplexNumber pow = new DoubleComplexNumber(-2, 0);
		LOG.info(val.toString()+" ^ ("+pow.toString()+")");
		int steps = 1 << 2;
		LOG.info("steps: "+steps);
		long t1 = System.nanoTime();
		for (int i = 0 ; i < steps ; i++)
			val.pow(pow);
		long t2 = System.nanoTime();
		LOG.info(val.toString());
		LOG.info("time: "+NumberUtil.NS_TO_MS*(t2-t1)+" ms");
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
		if (other.real == 0 && other.imag == 0) {
			//z^0 = 1
			//Technically the method should throw an exception if _this_ is 0 as well since 0^0 is undefined.
			//This edge case should be irrelevant for Fractals though, so it returns 1 if the exponent is 0
			real = 1;
			imag = 0;
			return;
		}
		if (other.imag == 0) {
			if (other.real == 2) //just squared
				square();
//			else if (other.real%1 == 0) { //faster integer exponentiation
//				double powLeft = other.real;
//				boolean negativePower = powLeft < 0;
//				if (negativePower) {
//					if (real == 0 && imag == 0) //Same principle as above but for this since x^-n = 1/x^n 
//						return;
//					powLeft = -powLeft;
//				}
//				
//				int squarePow = 2;
//				List<DoubleComplexNumber> pow2Buffer = new ArrayList<>();
//				
//				//square as long as possible
//				for ( ; squarePow <= powLeft ; ) {
//					pow2Buffer.add(copy());
//					square();
//					squarePow <<= 1;
//				}
//				
//				powLeft -= squarePow >> 1;
//				if (powLeft == 0) {
//					if (negativePower) {
//						reciprocal();
//					}
//					return;
//				}
//				
//				//add intermediate results
//				int powStep = 1 << (pow2Buffer.size()-1);
//				for (int i = pow2Buffer.size()-1 ; i >= 0 ; i--) {
//					if (powLeft >= powStep) {
//						mult(pow2Buffer.get(i));
//						powLeft -= powStep;
//						if (powLeft == 0) {
//							if (negativePower) {
//								reciprocal();
//							}
//							return;
//						}
//					}
//					powStep >>= 1;
//				}
//			}
			else {
				double abs = absDouble();
				double logAbs = Math.log(abs);
				double angle = Math.atan2(imag, real);
				//mult
				logAbs = logAbs*other.real;
				angle = angle*other.real;
				//exp
				double factor = Math.exp(logAbs);
				real = factor * Math.cos(angle);
				imag = factor * Math.sin(angle);
			}
		}
		else { //complex power
			//log().multiply(x).exp()
			//log
			double abs = absDouble();
			double logAbs = 0;
			if (abs != 0)
				logAbs = Math.log(abs);
			double angle = Math.atan2(imag, real);
			//mult
			double temp = logAbs*other.real - angle*other.imag;
			angle = logAbs*other.imag + angle*other.real;
			logAbs = temp;
			//exp
			double factor = Math.exp(logAbs);
			real = factor * Math.cos(angle);
			imag = factor * Math.sin(angle);
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
	
	/**
	 * 
	 * inverse result by multiplying with complex conjugate
	 *  1   1*conj(z)   conj(z)
	 *  - = --------- = ---------
	 *  z   z*conj(z)   real(z)^2+imag(z)^2
	 */
	@Override
	public void reciprocal() {
		double scale = real*real + imag*imag;
		if (scale == 0) {
			real = real == 0 ? 0 : real > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
			imag = imag == 0 ? 0 : imag > 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
			return;
		}
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
