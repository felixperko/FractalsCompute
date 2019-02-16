package de.felixperko.fractals.system.calculator.infra;

import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public interface FractalsCalculator{
	public void calculate(Chunk chunk);
	public boolean isCancelled();
	public void setCancelled();
	public void setParams(Map<String, ParamSupplier> parameters);
}
