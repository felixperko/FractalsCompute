package de.felixperko.fractals.system.systems.infra;

import java.util.Map;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public interface SystemContext {

	boolean setParameters(Map<String, ParamSupplier> params);

}