package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewData;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Nestable;
import de.felixperko.fractals.util.NestedMap;

public class BreadthFirstViewData implements ViewData {
	private static final long serialVersionUID = -6980552871281336220L;

	CategoryLogger log = CategoryLogger.WARNING.createSubLogger("calc/taskmanager/bf_data");
	
	SystemContext systemContext;
	
	transient Map<Integer, Map<Integer, Chunk>> chunks = new HashMap<>(); //key1 = chunkX, key2 = chunkY, value = chunk
	Nestable<Integer, CompressedChunk> chunks_compressed = new NestedMap<>(); //key1 = chunkX, key2 = chunkY, value = compressedChunk
	
	ComplexNumber anchor;
	
	public BreadthFirstViewData(ComplexNumber anchor) {
		this.anchor = anchor;
	}
	
	public boolean insertCompressedChunk(CompressedChunk compressedChunk) {
		return this.chunks_compressed.getOrMakeChild(compressedChunk.getChunkX()).getOrMakeChild(compressedChunk.getChunkY()).setValue(compressedChunk);
	}
	
	public CompressedChunk getCompressedChunk(Integer chunkX, Integer chunkY) {
		return chunks_compressed.getChild(chunkX).getChild(chunkY).getValue();
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
		chunks_compressed.clear();
	}

	@Override
	public ComplexNumber getAnchor() {
		return anchor;
	}
}
