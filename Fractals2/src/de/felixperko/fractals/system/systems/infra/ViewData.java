package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public interface ViewData extends Serializable{

	void addChunk(Chunk chunk);
	Chunk getChunk(Integer chunkX, Integer chunkY);
	boolean hasChunk(Integer chunkX, Integer chunkY);
	
	ComplexNumber getAnchor();

	void dispose();

}