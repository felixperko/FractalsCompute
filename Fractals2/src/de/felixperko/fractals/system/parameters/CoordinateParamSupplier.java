package de.felixperko.fractals.system.parameters;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;

public class CoordinateParamSupplier extends MappedParamSupplier {
	
	NumberFactory numberFactory;
	
	public CoordinateParamSupplier(String name, NumberFactory numberFactory) {
		super(name);
		this.numberFactory = numberFactory;
	}
	
	ParamSupplier p_pixelzoom;
	ParamSupplier p_chunkpos;
	ParamSupplier p_chunksize;
	
	@Override
	public Object get(int pixel) {
		int chunkSize = (Integer)p_chunksize.get(0);
		int x = pixel/chunkSize;
		int y = pixel%chunkSize;
		ComplexNumber chunkPos = (ComplexNumber)p_chunkpos.get(0);
		Number pixelzoom = (Number)p_pixelzoom.get(0);
		ComplexNumber n = numberFactory.createComplexNumber(x, y);
		n.multNumber(pixelzoom);
		n.add(chunkPos);
		return n;
	}

}
