package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Collection;
import java.util.LinkedList;

import de.felixperko.fractals.system.AbstractViewContainer;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class BFViewContainer extends AbstractViewContainer<BreadthFirstViewData> {

	LinkedList<BreadthFirstViewData> oldViewData = new LinkedList<>();
	int oldViewBufferSize;
	
	public BFViewContainer(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
	}
	
	@Override
	public void setActiveViewData(BreadthFirstViewData viewData) {
		if (viewData == this.activeViewData)
			return;
		storeCurrentActiveViewData();
		super.setActiveViewData(viewData);
	}
	
	public void reactivateViewData(BreadthFirstViewData oldViewData) {
		boolean removed = this.oldViewData.remove(oldViewData);
		if (!removed)
			throw new IllegalArgumentException("tried to reactivate a ViewData that isn't in the deactivated list");
		setActiveViewData(oldViewData);
	}
	
	public void setOldViewBufferSize(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
		enforceBufferSize();
	}

	public Collection<BreadthFirstViewData> getInactiveViews() {
		return oldViewData;
	}

	void storeCurrentActiveViewData() {
		if (activeViewData == null)
			return;
		oldViewData.addFirst(activeViewData);
		activeViewData.clearBufferedChunks();
		enforceBufferSize();
	}

	private void enforceBufferSize() {
		while (oldViewData.size() > oldViewBufferSize)
			disposeLastOldViewData();
	}

	private void disposeLastOldViewData() {
		ViewData<?> old = oldViewData.removeLast();
		old.dispose();
	}

}
