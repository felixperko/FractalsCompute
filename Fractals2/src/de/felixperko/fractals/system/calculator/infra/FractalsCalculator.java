package de.felixperko.fractals.system.calculator.infra;

import java.io.Serializable;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public interface FractalsCalculator extends Serializable{
	public void calculate(AbstractArrayChunk chunk);
	public boolean isCancelled();
	public void setCancelled();
	public void setContext(SystemContext systemContext);
}
