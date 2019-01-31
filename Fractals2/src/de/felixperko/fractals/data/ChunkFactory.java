package de.felixperko.fractals.data;

public class ChunkFactory {
	
	int dimensionSize;
	
	public ChunkFactory(int dimensionSize) {
		this.dimensionSize = dimensionSize;
	}
	
	public Chunk createChunk(int chunkX, int chunkY) {
		return new Chunk(chunkX, chunkY, dimensionSize);
	}
}
