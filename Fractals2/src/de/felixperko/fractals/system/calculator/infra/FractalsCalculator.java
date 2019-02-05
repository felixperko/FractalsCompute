package de.felixperko.fractals.system.calculator.infra;

import de.felixperko.fractals.data.Chunk;

public interface FractalsCalculator{
	public void calculate(Chunk chunk);
	public boolean isCancelled();
	public void setCancelled();
}
