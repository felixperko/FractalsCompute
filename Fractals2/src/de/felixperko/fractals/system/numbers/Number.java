package de.felixperko.fractals.system.numbers;

import java.io.Serializable;

public interface Number<N extends Number<N>> extends Serializable{
	
	public void add(N other);
	public void sub(N other);
	public void mult(N other);
	public void div(N other);
	public void pow(N other);
	public void square();
	
	public double toDouble();
	public String toString();
	public N copy();
}