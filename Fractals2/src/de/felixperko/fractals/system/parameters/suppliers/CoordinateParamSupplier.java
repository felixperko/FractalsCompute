package de.felixperko.fractals.system.parameters.suppliers;

import java.util.Random;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;

public abstract class CoordinateParamSupplier extends MappedParamSupplier {
	
	private static final long serialVersionUID = -8190334359234597063L;
	
	public CoordinateParamSupplier(String name) {
		super(name);
	}
	
	ParamSupplier p_pixelzoom;
	ParamSupplier p_chunkpos;
	ParamSupplier p_chunksize;
	
	public abstract void applyRelativeSampleShift(ComplexNumber pixelCoord, int sample);
}
