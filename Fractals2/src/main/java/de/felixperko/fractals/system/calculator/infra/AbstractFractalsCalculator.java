package de.felixperko.fractals.system.calculator.infra;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;

public abstract class AbstractFractalsCalculator implements FractalsCalculator {

	private static final long serialVersionUID = 448587812207550305L;
	
	protected SystemContext<? extends ViewContainer<?>> systemContext;
	
	boolean cancelled = false;
	
	protected boolean trace = false;
	protected List<TraceListener> traceListeners = new ArrayList<>();
	
	public AbstractFractalsCalculator() {
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
	
	@Override
	public void setTrace(boolean trace) {
		this.trace = trace;
	}
	
	@Override
	public void addTraceListener(TraceListener traceListener) {
		this.traceListeners.add(traceListener);
	}
	
	@Override
	public void removeTraceListener(TraceListener traceListener) {
		this.traceListeners.remove(traceListener);
	}
}
