package de.felixperko.fractals.system.systems.infra;

import java.util.Collection;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstViewData;

public interface ViewContainer extends ViewContainerListener {

	boolean registerViewContainerListener(ViewContainerListener listener);

	boolean unregisterViewContainerListener(ViewContainerListener listener);

	BreadthFirstViewData getActiveViewData();

	void setActiveViewData(BreadthFirstViewData viewData);

	void setOldViewBufferSize(int oldViewBufferSize);

	Collection<BreadthFirstViewData> getInactiveViews();

	void reactivateViewData(BreadthFirstViewData oldViewData);

}