package de.felixperko.fractals.system.Numbers;

import de.felixperko.fractals.system.Numbers.infra.AbstractComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public class DoubleComplexNumber extends AbstractComplexNumber<DoubleNumber, DoubleComplexNumber>{
	
	private static final long serialVersionUID = -6625226042922366712L;
	
	public static void main(String[] args) {
		DoubleComplexNumber n1 = new DoubleComplexNumber();
		DoubleComplexNumber n2 = new DoubleComplexNumber();
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
	public void pow(DoubleComplexNumber other) {
//		if (other.imag == 0) {
//			if (other.real == 2) //just squared
//				square();
//			else { //real power
//				double modulus = Math.sqrt(real*real + imag*imag);
//			    double arg = Math.atan2(imag,real);
//			    double log_re = Math.log(modulus);
//			    double log_im = arg;
//			    double x_log_re = other.imag * log_re;
//			    double x_log_im = other.imag * log_im;
//			    double modulus_ans = Math.exp(x_log_re);
//			    real = modulus_ans*Math.cos(x_log_im);
//			    imag = modulus_ans*Math.sin(x_log_im);
//			}
		if (other.imag == 0 && other.real == 2) {
			square();
		} else { //complex power
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
	public double toDouble() {
		// TODO Auto-generated method stub
		return real;
	}
	
	@Override
	public String toString() {
		return real+","+imag;
	}

	@Override
	public ComplexNumber copy() {
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
		//double den = real*real + imag*imag;
//		double newReal = (real*otherComplex.real + imag*otherComplex.imag)/den;
//		imag = (imag*otherComplex.real - real*otherComplex.imag)/den;
//		real = newReal;
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
	public double realImag() {
		return imag;
	}
}
