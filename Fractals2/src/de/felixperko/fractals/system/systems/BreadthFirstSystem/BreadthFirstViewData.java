package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.NaiveChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.util.CategoryLogger;

public class BreadthFirstViewData implements ViewData {
	CategoryLogger log = CategoryLogger.WARNING.createSubLogger("calc/taskmanager/bf_data");
	
	Map<Integer, Map<Integer, Chunk>> chunks = new HashMap<>();
	
	ComplexNumber anchor;
	
	public BreadthFirstViewData(ComplexNumber anchor) {
		this.anchor = anchor;
	}
	
	@Override
	public void addChunk(Chunk chunk) {
		Map<Integer, Chunk> xMap = getXMap(chunk.getChunkX());
		if (xMap.containsKey(chunk.getChunkY()))
			log.log("overwriting existing chunk");
		xMap.put(chunk.getChunkY(), chunk);
	}
	
	@Override
	public Chunk getChunk(Integer chunkX, Integer chunkY) {
		Chunk c = getXMap(chunkX).get(chunkY);
		return c;
	}
	
	private Map<Integer, Chunk> getXMap(Integer chunkX){
		Map<Integer, Chunk> ans = chunks.get(chunkX);
		if (ans == null) {
			ans = new HashMap<Integer, Chunk>();
			chunks.put(chunkX, ans);
		}
		return ans;
	}

	@Override
	public boolean hasChunk(Integer chunkX, Integer chunkY) {
		Map<Integer, Chunk> map = chunks.get(chunkX);
		if (map == null)
			return false;
		return map.get(chunkY) != null;
	}

	@Override
	public void dispose() {
		chunks.clear();
	}
}
