package de.felixperko.fractals.system.parameters;

import java.util.Random;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;

public abstract class CoordinateParamSupplier extends MappedParamSupplier {
	
	NumberFactory numberFactory;
	
	public CoordinateParamSupplier(String name, NumberFactory numberFactory) {
		super(name);
		this.numberFactory = numberFactory;
	}
	
	ParamSupplier p_pixelzoom;
	ParamSupplier p_chunkpos;
	ParamSupplier p_chunksize;
	
	@Override
	public Object get(int pixel, int sample) {
		int chunkSize = (Integer)p_chunksize.get(0, 0);
		int x = pixel/chunkSize;
		int y = pixel%chunkSize;
		ComplexNumber chunkPos = (ComplexNumber)p_chunkpos.get(0, 0);
		Number pixelzoom = (Number)p_pixelzoom.get(0, 0);
		ComplexNumber n = numberFactory.createComplexNumber(x, y);
		applyRelativeSampleShift(n, sample);
		n.multNumber(pixelzoom);
		n.add(chunkPos);
		return n;
	}
	
	public abstract void applyRelativeSampleShift(ComplexNumber pixelCoord, int sample);
}
