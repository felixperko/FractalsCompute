package de.felixperko.fractals.system.parameters.suppliers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.LayerConfiguration;

public class CoordinateBasicShiftParamSupplier extends CoordinateParamSupplier {
	
	private static final long serialVersionUID = 2317887367642326504L;
	
	ComplexNumber[] shifts;
	LayerConfiguration layerConfiguration;
//	int dim = 0;
	
//	public CoordinateBasicShiftParamSupplier(String name, NumberFactory numberFactory, int dim) {
//		super(name, numberFactory);
////		this.dim = dim;
//		shifts = new ComplexNumber[dim*dim];
//		for (float x = 0 ; x < dim ; x++){
//			for (float y = 0 ; y < dim ; y++){
//				shifts[Math.round(x*dim+y)] = numberFactory.createComplexNumber(x/dim, y/dim);
//			}
//		}
//	}
	
	public CoordinateBasicShiftParamSupplier(String name) {
		super(name);
	}
	
	public CoordinateBasicShiftParamSupplier(String name, LayerConfiguration layerConfiguration) {
		super(name);
		this.layerConfiguration = layerConfiguration;
		if (this.layerConfiguration != null) {
			List<ComplexNumber> shifts = new ArrayList<>();
			for (int i = 0 ; i < layerConfiguration.getLayers().size() ; i++) {
				for (ComplexNumber n : layerConfiguration.getOffsets(i))
					shifts.add(n);
			}
			this.shifts = shifts.toArray(new ComplexNumber[shifts.size()]);
		}
	}
	
//	private CoordinateBasicShiftParamSupplier(String name, NumberFactory numberFactory, ComplexNumber[] shifts) {
//		super(name, numberFactory);
////		this.dim = Math.round((float)Math.sqrt(shifts.length));
//		this.shifts = shifts;
//	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof CoordinateBasicShiftParamSupplier))
			return false;
		
		
		return true;
	}
	
	ParamSupplier p_pixelzoom;
	ParamSupplier p_chunkpos;
	ParamSupplier p_chunksize;
	ParamSupplier p_layer;
	
	@Override
	public void bindParameters(Map<String, ParamSupplier> parameters) {
		super.bindParameters(parameters);
		if (layerConfiguration == null) {
			this.layerConfiguration = parameters.get("layerConfiguration").getGeneral(LayerConfiguration.class);
			List<ComplexNumber> shifts = new ArrayList<>();
			for (int i = 0 ; i < layerConfiguration.getLayers().size() ; i++) {
				for (ComplexNumber n : layerConfiguration.getOffsets(i))
					shifts.add(n);
			}
			this.shifts = shifts.toArray(new ComplexNumber[shifts.size()]);
		}
	}
	
	@Override
	public Object get(int pixel, int sample) {
		int chunkSize = (Integer)p_chunksize.get(0, 0);
		int x = pixel/chunkSize;
		int y = pixel%chunkSize;
		ComplexNumber chunkPos = (ComplexNumber)p_chunkpos.get(0, 0);
		Number pixelzoom = (Number)p_pixelzoom.get(0, 0);
		ComplexNumber n = layerConfiguration.getNumberFactory().createComplexNumber(x, y);
		applyRelativeSampleShift(n, sample);
		n.multNumber(pixelzoom);
		n.add(chunkPos);
		return n;
	}

	@Override
	public ParamSupplier copy() {
		//TODO remove recopy at copy!
		return new CoordinateBasicShiftParamSupplier(name, layerConfiguration);
	}

	@Override
	public void applyRelativeSampleShift(ComplexNumber pixelCoord, int sample) {
		pixelCoord.add(shifts[sample]);
	}

	
	@Override
	public boolean evaluateChanged(ParamSupplier old) {
		if (old == null)
			return true;
		return !this.equals(old);
	}


}
