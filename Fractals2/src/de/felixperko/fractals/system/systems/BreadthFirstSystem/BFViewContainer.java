package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.system.systems.infra.ViewContainer;
import de.felixperko.fractals.system.systems.infra.ViewContainerListener;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class BFViewContainer implements ViewContainer {

	BreadthFirstViewData activeViewData;
	LinkedList<BreadthFirstViewData> oldViewData = new LinkedList<>();
	int oldViewBufferSize;
	
	List<ViewContainerListener> listeners = new ArrayList<>();
	
	public BFViewContainer(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
	}
	
	@Override
	public void registerViewContainerListener(ViewContainerListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public boolean unregisterViewContainerListener(ViewContainerListener listener) {
		return listeners.remove(listener);
	}
	
	@Override
	public BreadthFirstViewData getActiveViewData() {
		return activeViewData;
	}
	
	@Override
	public void setActiveViewData(BreadthFirstViewData viewData) {
		if (viewData == this.activeViewData)
			return;
		storeCurrentActiveViewData();
		if (this.activeViewData != null)
			this.activeViewData.setActive(false);
		this.activeViewData = viewData;
		if (viewData != null)
			viewData.setActive(true);
		activeViewChanged(viewData);
	}
	
	@Override
	public void reactivateViewData(BreadthFirstViewData oldViewData) {
		boolean removed = this.oldViewData.remove(oldViewData);
		if (!removed)
			throw new IllegalArgumentException("tried to reactivate a ViewData that isn't in the deactivated list");
		setActiveViewData(oldViewData);
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
		ViewData old = oldViewData.removeLast();
		old.dispose();
	}
	
	@Override
	public void setOldViewBufferSize(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
		enforceBufferSize();
	}

	@Override
	public void activeViewChanged(ViewData activeView) {
		for (ViewContainerListener listener : listeners)
			listener.activeViewChanged(activeView);
	}

	@Override
	public void insertedBufferedChunk(Chunk chunk, ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.insertedBufferedChunk(chunk, viewData);
	}

	@Override
	public void updatedBufferedChunk(Chunk chunk, ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.updatedBufferedChunk(chunk, viewData);
	}

	@Override
	public void removedBufferedChunk(Integer chunkX, Integer chunkY, ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.removedBufferedChunk(chunkX, chunkY, viewData);
	}

	@Override
	public void clearedBufferedChunks(ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.clearedBufferedChunks(viewData);
	}

	@Override
	public void insertedCompressedChunk(CompressedChunk chunk, ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.insertedCompressedChunk(chunk, viewData);
	}

	@Override
	public void updatedCompressedChunk(CompressedChunk chunk, ViewData viewData) {

		for (ViewContainerListener listener : listeners)
			listener.updatedCompressedChunk(chunk, viewData);
	}

	@Override
	public void removedCompressedChunk(Integer chunkX, Integer chunkY, ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.removedCompressedChunk(chunkX, chunkY, viewData);
	}

	@Override
	public void clearedCompressedChunks(ViewData viewData) {
		for (ViewContainerListener listener : listeners)
			listener.clearedCompressedChunks(viewData);
	}

	@Override
	public void disposedViewData() {
		for (ViewContainerListener listener : listeners)
			listener.disposedViewData();
	}

	@Override
	public Collection<BreadthFirstViewData> getInactiveViews() {
		return oldViewData;
	}

}
