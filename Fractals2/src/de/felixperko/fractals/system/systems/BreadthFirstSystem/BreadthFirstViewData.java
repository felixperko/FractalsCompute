package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.util.CategoryLogger;

public class BreadthFirstViewData {
	CategoryLogger log = CategoryLogger.WARNING.createSubLogger("calc/taskmanager/bf_data");
	
	Map<Long, Map<Long, Chunk>> chunks = new HashMap<>();
	
	ComplexNumber anchor;
	
	public BreadthFirstViewData(ComplexNumber anchor) {
		this.anchor = anchor;
	}
	
	public void addChunk(Chunk chunk) {
		Map<Long, Chunk> xMap = getXMap(chunk.getChunkX());
		if (xMap.containsKey(chunk.getChunkY()))
			log.log("overwriting existing chunk");
		xMap.put(chunk.getChunkY(), chunk);
	}
	
	public Chunk getChunk(Long chunkX, Long chunkY) {
		Chunk c = getXMap(chunkX).get(chunkY);
		return c;
	}
	
	private Map<Long, Chunk> getXMap(Long chunkX){
		Map<Long, Chunk> ans = chunks.get(chunkX);
		if (ans == null) {
			ans = new HashMap<Long, Chunk>();
			chunks.put(chunkX, ans);
		}
		return ans;
	}

	public boolean hasChunk(Long chunkX, Long chunkY) {
		Map<Long, Chunk> map = chunks.get(chunkX);
		if (map == null)
			return false;
		return map.get(chunkY) != null;
	}

	
	public void dispose() {
		chunks.clear();
	}
}
