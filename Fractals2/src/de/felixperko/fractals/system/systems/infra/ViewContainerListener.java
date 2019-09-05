package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;

public interface ViewContainerListener {
	
	void activeViewChanged(ViewData activeView);
	
	void insertedBufferedChunk(Chunk chunk, ViewData viewData);
	void updatedBufferedChunk(Chunk chunk, ViewData viewData);
	void removedBufferedChunk(Integer chunkX, Integer chunkY, ViewData viewData);
	void clearedBufferedChunks(ViewData viewData);

	void insertedCompressedChunk(CompressedChunk compressedChunk, ViewData viewData);
	void updatedCompressedChunk(CompressedChunk compressedChunk, ViewData viewData);
	void removedCompressedChunk(Integer chunkX, Integer chunkY, ViewData viewData);
	void clearedCompressedChunks(ViewData viewData);
	
	void disposedViewData();
}
