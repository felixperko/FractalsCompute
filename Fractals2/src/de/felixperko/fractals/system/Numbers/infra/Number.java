package de.felixperko.fractals.system.Numbers.infra;

import java.io.Serializable;

public interface Number<T> extends Serializable{
	
	public void add(T other);
	public void mult(T other);
	public void pow(T other);
	public void square();
	
	public double toDouble();
	public String toString();
}
