package de.felixperko.fractals.system.systems.infra;

public interface ViewContainer<D extends ViewData> extends ViewContainerListener {

	boolean registerViewContainerListener(ViewContainerListener listener);
	boolean unregisterViewContainerListener(ViewContainerListener listener);

	D getActiveViewData();
	void setActiveViewData(D viewData);

}