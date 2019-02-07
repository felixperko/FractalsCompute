package de.felixperko.fractals.network;

import de.felixperko.fractals.data.Chunk;

public interface ClientSystemInterface {
	public void chunkUpdated(Chunk chunk);
	public void chunksCleared();
}
