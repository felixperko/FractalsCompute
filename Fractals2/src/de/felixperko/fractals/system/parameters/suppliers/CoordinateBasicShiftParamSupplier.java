package de.felixperko.fractals.system.parameters.suppliers;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public class CoordinateBasicShiftParamSupplier extends MappedParamSupplier {
	
	private static final long serialVersionUID = 2317887367642326504L;
	
	public CoordinateBasicShiftParamSupplier(String name) {
		super(name);
	}
	
	@Override
	public Object get(SystemContext systemContext, ComplexNumber chunkPos, int pixel, int sample) {
		int chunkSize = systemContext.getChunkSize();
		int x = pixel/chunkSize;
		int y = pixel%chunkSize;
		ComplexNumber n = systemContext.getNumberFactory().createComplexNumber(x, y);
		ComplexNumber offset = systemContext.getLayerConfiguration().getOffsetForSample(sample);
		n.add(offset);
		Number pixelzoom = systemContext.getPixelzoom();
		n.multNumber(pixelzoom);
		n.add(chunkPos);
		return n;
	}

	@Override
	public ParamSupplier copy() {
		return new CoordinateBasicShiftParamSupplier(name);
	}

	@Override
	public boolean evaluateChanged(ParamSupplier old) {
		if (old == null)
			return true;
		return !this.equals(old);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof CoordinateBasicShiftParamSupplier))
			return false;
		return true;
	}
}
