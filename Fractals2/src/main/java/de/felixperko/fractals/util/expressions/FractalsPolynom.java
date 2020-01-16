package de.felixperko.fractals.util.expressions;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.SystemContext;

/**
 * f(x) = ax^b + cx^d + ex^f + ...
 */

public class FractalsPolynom {
	
	ParamSupplier[] factors;
	ParamSupplier[] powers;
	int steps;
	int factorsLength;
	int powersLength;
	
	public FractalsPolynom(ParamSupplier[] factors, ParamSupplier[] powers) {
		this.factors = factors;
		this.powers = powers;
		this.factorsLength = factors.length;
		this.powersLength = powers.length;
		this.steps = Math.max(factorsLength, powersLength);
	}
	
	public void evaluate(SystemContext<?> systemContext, ComplexNumber chunkPos, int pixel, int sample, ComplexNumber x, ComplexNumber y) {
		for (int i = 0 ; i < steps ; i++) {
			ComplexNumber current = x.copy();
			ComplexNumber pow = (ComplexNumber) powers[i].get(systemContext, chunkPos, pixel, sample);
			if (powersLength > i) {
				current.pow(pow);
			}
			if (factorsLength > i) {
				current.mult((ComplexNumber)factors[i].get(systemContext, chunkPos, pixel, sample));
			}
			y.add(current);
		}
		
	}
}
