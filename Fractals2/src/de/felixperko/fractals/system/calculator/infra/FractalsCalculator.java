package de.felixperko.fractals.system.calculator.infra;

import java.io.Serializable;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;

public interface FractalsCalculator extends Serializable{
	public void calculate(AbstractArrayChunk chunk, IStats taskStats);
	public boolean isCancelled();
	public void setCancelled();
	public void setContext(SystemContext<? extends ViewContainer<?>> systemContext);
	
	public void setTrace(boolean trace);
	public void addTraceListener(TraceListener traceListener);
	public void removeTraceListener(TraceListener traceListener);
}
