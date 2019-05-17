package de.felixperko.fractals.system.parameters.suppliers;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

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
