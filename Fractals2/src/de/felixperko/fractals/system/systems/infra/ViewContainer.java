package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstViewData;

public interface ViewContainer extends ViewContainerListener {

	void registerViewContainerListener(ViewContainerListener listener);

	boolean unregisterViewContainerListener(ViewContainerListener listener);

	BreadthFirstViewData getActiveViewData();

	void setActiveViewData(BreadthFirstViewData viewData);

	void setOldViewBufferSize(int oldViewBufferSize);

}