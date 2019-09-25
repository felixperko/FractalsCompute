package de.felixperko.fractals.system;

import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;

public interface ZoomableSystemContext<C extends ViewContainer> extends SystemContext<C> {

	Number getZoom();

	void setZoom(Number zoom);

}