package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.data.Chunk;

public interface SingleViewData<CONTEXT> extends ViewData<CONTEXT> {
	Chunk getChunk();
	void setChunk(Chunk chunk);
}
