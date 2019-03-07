package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import de.felixperko.fractals.data.Chunk;

public interface ViewData {

	void addChunk(Chunk chunk);

	Chunk getChunk(Integer chunkX, Integer chunkY);

	boolean hasChunk(Integer chunkX, Integer chunkY);

	void dispose();

}