package de.felixperko.fractals.system.calculator.infra;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.parameters.suppliers.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public abstract class AbstractFractalsCalculator implements FractalsCalculator {

	private static final long serialVersionUID = 448587812207550305L;

	transient Map<String, Field> fields = null;
	
	SystemContext systemContext;
	
	Class<? extends AbstractFractalsCalculator> fieldClass;
	
	boolean cancelled = false;
	
	public AbstractFractalsCalculator(Class<? extends AbstractFractalsCalculator> fieldClass) {
		this.fieldClass = fieldClass;
	}
	
	@Override
	public void setContext(SystemContext systemContext) {
		this.systemContext = systemContext;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled() {
		cancelled = true;
	}
}
