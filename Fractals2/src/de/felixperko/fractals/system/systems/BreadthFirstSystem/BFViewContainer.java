package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.LinkedList;
import java.util.List;

public class BFViewContainer {
	
	int oldViewBufferSize;
	
	public BFViewContainer(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
	}
	
	BreadthFirstViewData activeViewData;
	LinkedList<BreadthFirstViewData> oldViewData = new LinkedList<>();
	
	public BreadthFirstViewData getActiveViewData() {
		return activeViewData;
	}
	
	public void setActiveViewData(BreadthFirstViewData viewData) {
		storeCurrentActiveViewData();
		this.activeViewData = viewData;
	}

	private void storeCurrentActiveViewData() {
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
		BreadthFirstViewData old = oldViewData.removeLast();
		old.dispose();
	}
	
	public void setOldViewBufferSize(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
		enforceBufferSize();
	}
}
