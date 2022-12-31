package de.felixperko.fractals.io;

import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateDiscreteParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateModuloParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

public class ParamSupplierTypeRegistry extends ClassKeyRegistry<ParamSupplier> {
	
	@Override
	protected void initDefaultClassKeys() {
		
		classKeys.put(StaticParamSupplier.class, "s");
		classKeys.put(CoordinateBasicShiftParamSupplier.class, "c");
		classKeys.put(CoordinateDiscreteParamSupplier.class, "gs");
		classKeys.put(CoordinateModuloParamSupplier.class, "gc");
		
	}
}
