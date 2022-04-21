package de.felixperko.fractals.system;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.system.systems.infra.ViewContainer;
import de.felixperko.fractals.system.systems.infra.ViewContainerListener;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class AbstractViewContainer<D extends ViewData> implements ViewContainer<D> {

	protected D activeViewData;
	List<ViewContainerListener> listeners = new ArrayList<>();

	public AbstractViewContainer() {
		super();
	}

	@Override
	public boolean registerViewContainerListener(ViewContainerListener listener) {
		if (listeners.contains(listener))
			return false;
		listeners.add(listener);
		return true;
	}

	@Override
	public boolean unregisterViewContainerListener(ViewContainerListener listener) {
		return listeners.remove(listener);
	}

	@Override
	public D getActiveViewData() {
		return activeViewData;
	}

	@Override
	public void setActiveViewData(D viewData) {
		if (viewData == this.activeViewData)
			return;
		if (this.activeViewData != null)
			this.activeViewData.setActive(false);
		this.activeViewData = viewData;
		if (viewData != null)
			viewData.setActive(true);
		activeViewChanged(viewData);
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

}