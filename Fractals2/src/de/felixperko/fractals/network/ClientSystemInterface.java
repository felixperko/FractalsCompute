package de.felixperko.fractals.network;

import de.felixperko.fractals.data.AbstractArrayChunk;

public interface ClientSystemInterface {
	public void chunkUpdated(AbstractArrayChunk chunk);
	public void chunksCleared();
}
