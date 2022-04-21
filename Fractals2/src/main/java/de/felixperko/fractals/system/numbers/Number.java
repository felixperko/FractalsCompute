package de.felixperko.fractals.system.numbers;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonValue;

import de.felixperko.fractals.util.serialization.jackson.JsonValueWrapper;

public interface Number<N extends Number<N>> extends Serializable{
	
	public void add(N other);
	public void sub(N other);
	public void mult(N other);
	public void div(N other);
	public void pow(N other);
	public void mod(N other);
	public void square();
	
	public double toDouble();
	public String toString();
	@JsonValue
	public JsonValueWrapper toWrappedValue();
	public N copy();
}
