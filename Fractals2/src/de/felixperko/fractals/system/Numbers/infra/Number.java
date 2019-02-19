package de.felixperko.fractals.system.Numbers.infra;

import java.io.Serializable;

public interface Number<N> extends Serializable{
	
	public void add(N other);
	public void mult(N other);
	public void pow(N other);
	public void square();
	
	public double toDouble();
	public String toString();
}
