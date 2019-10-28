package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;

public class ViewContainerAdapter implements ViewContainerListener {

	@Override
	public void activeViewChanged(ViewData activeView) {

	}

	@Override
	public void insertedBufferedChunk(Chunk chunk, ViewData viewData) {

	}

	@Override
	public void updatedBufferedChunk(Chunk chunk, ViewData viewData) {

	}

	@Override
	public void removedBufferedChunk(Integer chunkX, Integer chunkY, ViewData viewData) {

	}

	@Override
	public void clearedBufferedChunks(ViewData viewData) {

	}

	@Override
	public void insertedCompressedChunk(CompressedChunk compressedChunk, ViewData viewData) {

	}

	@Override
	public void updatedCompressedChunk(CompressedChunk compressedChunk, ViewData viewData) {

	}

	@Override
	public void removedCompressedChunk(Integer chunkX, Integer chunkY, ViewData viewData) {

	}

	@Override
	public void clearedCompressedChunks(ViewData viewData) {

	}

	@Override
	public void disposedViewData() {

	}

}
