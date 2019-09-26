package de.felixperko.fractals.system.calculator.infra;

import java.lang.reflect.Field;
import java.util.Map;

import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;

public abstract class AbstractFractalsCalculator implements FractalsCalculator {

	private static final long serialVersionUID = 448587812207550305L;

	transient Map<String, Field> fields = null;
	
	SystemContext<? extends ViewContainer<?>> systemContext;
	
	Class<? extends AbstractFractalsCalculator> fieldClass;
	
	boolean cancelled = false;
	
	public AbstractFractalsCalculator(Class<? extends AbstractFractalsCalculator> fieldClass) {
		this.fieldClass = fieldClass;
	}
	
	@Override
	public void setContext(SystemContext<? extends ViewContainer<?>> systemContext) {
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
