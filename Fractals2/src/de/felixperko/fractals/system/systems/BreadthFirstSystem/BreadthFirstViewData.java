package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public class BreadthFirstViewData {
	Map<Long, Map<Long, Chunk>> chunks = new HashMap<>();
	ComplexNumber anchor;
	
	
	public boolean hasChunk(long chunkX, long chunkY) {
		Map<Long, Chunk> map = chunks.get(chunkX);
		if (map == null)
			return false;
		return map.get(chunkY) != null;
	}
}
