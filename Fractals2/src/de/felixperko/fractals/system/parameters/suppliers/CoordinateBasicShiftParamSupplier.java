package de.felixperko.fractals.system.parameters.suppliers;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.LayerConfiguration;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public class CoordinateBasicShiftParamSupplier extends MappedParamSupplier {
	
	private static final long serialVersionUID = 2317887367642326504L;
	
	public CoordinateBasicShiftParamSupplier(String name) {
		super(name);
	}
	
	@Override
	public Object get(SystemContext systemContext, ComplexNumber chunkPos, int pixel, int sample) {
		int chunkSize = systemContext.getParamValue("chunkSize", Integer.class);
		int x = pixel/chunkSize;
		int y = pixel%chunkSize;
		Number pixelzoom = systemContext.getParamValue("pixelzoom", Number.class);
		ComplexNumber n = systemContext.getParamValue("numberFactory", NumberFactory.class).createComplexNumber(x, y);
		ComplexNumber offset = systemContext.getParamValue("layerConfiguration", LayerConfiguration.class).getOffsetForSample(sample);
		n.add(offset);
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
