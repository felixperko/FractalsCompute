package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.util.Nestable;
import de.felixperko.fractals.util.NestedMap;
import de.felixperko.fractals.util.NestedNull;

public class BreadthFirstViewData extends AbstractBFViewData<BreadthFirstViewData> {
	
	private static final long serialVersionUID = -6980552871281336220L;

//	CategoryLogger log = CategoryLogger.WARNING.createSubLogger("calc/taskmanager/bf_data");
	
	ParamContainer paramContainer;
	
	transient Map<Integer, Map<Integer, Chunk>> chunks_buffered = new HashMap<>(); //key1 = chunkX, key2 = chunkY, value = chunk
	Nestable<Integer, CompressedChunk> chunks_compressed = new NestedMap<>(); //key1 = chunkX, key2 = chunkY, value = compressedChunk
	
	ComplexNumber anchor;
	
	public BreadthFirstViewData(ComplexNumber anchor) {
		this.anchor = anchor;
	}
	
	public void toPos(ComplexNumber gridPos, Number chunkZoom) {
		gridPos.multNumber(chunkZoom);
		gridPos.add(anchor);
	}
	
	public boolean insertCompressedChunkImpl(CompressedChunk compressedChunk) {
		return this.chunks_compressed.getOrMakeChild(compressedChunk.getChunkX()).getOrMakeChild(compressedChunk.getChunkY()).setValue(compressedChunk);
	}
	
	public CompressedChunk getCompressedChunk(Integer chunkX, Integer chunkY) {
		return chunks_compressed.getChild(chunkX).getChild(chunkY).getValue();
	}
	
	@Override
	public boolean insertBufferedChunkImpl(Chunk chunk, boolean insertCompressedChunk) {
		Map<Integer, Chunk> xMap = getXMap(chunk.getChunkX());
		boolean overwrite = xMap.containsKey(chunk.getChunkY());
		xMap.put(chunk.getChunkY(), chunk);
		lastSeenNow(chunk.getChunkX(), chunk.getChunkY());
		if (insertCompressedChunk){
			CompressedChunk compressedChunk = new CompressedChunk((ReducedNaiveChunk)chunk);
			insertCompressedChunk(compressedChunk, false);
		}
		return overwrite;
	}
	
	@Override
	public Chunk getBufferedChunk(Integer chunkX, Integer chunkY) {
		Chunk c = getXMap(chunkX).get(chunkY);
		if (c == null)
			c = loadBuffer(chunkX, chunkY);
		return c;
	}
	
	private Chunk loadBuffer(Integer chunkX, Integer chunkY) {
		CompressedChunk chunk = getCompressedChunk(chunkX, chunkY);
		if (chunk != null) {
			Chunk bufferedChunk = chunk.decompress();
			insertBufferedChunk(bufferedChunk, false);
			return bufferedChunk;
		}
		return null;
	}

	private Map<Integer, Chunk> getXMap(Integer chunkX){
		Map<Integer, Chunk> ans = chunks_buffered.get(chunkX);
		if (ans == null) {
			ans = new HashMap<Integer, Chunk>();
			chunks_buffered.put(chunkX, ans);
		}
		return ans;
	}

	@Override
	public boolean updateBufferedChunkImpl(Chunk chunk) {
		boolean overwrite;
		synchronized (chunks_buffered) {
			overwrite = hasBufferedChunk(chunk);
			getXMap(chunk.getChunkX()).put(chunk.getChunkY(), chunk);
		}
		return overwrite;
	}

	@Override
	public List<Chunk> getBufferedChunks() {
		List<Chunk> chunks = new ArrayList<>();
		synchronized (chunks_buffered) {
			for (Map<Integer, Chunk> xMap : new ArrayList<>(chunks_buffered.values()))
				chunks.addAll(xMap.values());
		}
		return chunks;
	}

	@Override
	public boolean hasBufferedChunk(Integer chunkX, Integer chunkY) {
		Map<Integer, Chunk> xMap = chunks_buffered.get(chunkX);
		if (xMap == null)
			return false;
		return xMap.containsKey(chunkY);
	}

	@Override
	public boolean removeBufferedChunkImpl(Integer chunkX, Integer chunkY, boolean removeCompressed) {
		boolean hasBuffered = !hasBufferedChunk(chunkX, chunkY);
		if (removeCompressed) {
			removeCompressedChunk(chunkX, chunkY);
			return true;
		}
		else {
			if (!hasBuffered)
				return false;
			synchronized (chunks_buffered) {
				Map<Integer, Chunk> xMap = getXMap(chunkX);
				xMap.remove(chunkY);
				if (xMap.isEmpty())
					chunks_buffered.remove(chunkX);
			}
			return true;
		}
	}

	@Override
	public void clearBufferedChunksImpl() {
		chunks_buffered.clear();
	}

	@Override
	public boolean insertCompressedChunkImpl(CompressedChunk compressedChunk, boolean insertBuffered) {
		boolean overwritten = chunks_compressed.getOrMakeChild(compressedChunk.getChunkX()).getOrMakeChild(compressedChunk.getChunkY()).setValue(compressedChunk);
		if (insertBuffered)
			insertBufferedChunk(compressedChunk.decompress(), false);
		return overwritten;
	}

	@Override
	public boolean updateCompressedChunkImpl(CompressedChunk compressedChunk, boolean updateBuffered) {
		return insertCompressedChunk(compressedChunk, updateBuffered);
	}

	@Override
	public List<CompressedChunk> getCompressedChunks() {
		List<CompressedChunk> list = new ArrayList<>();
		for (NestedMap<Integer, CompressedChunk> xMap : chunks_compressed.getChildren()) {
			for (NestedMap<Integer, CompressedChunk> yMap : xMap.getChildren()) {
				CompressedChunk compressedChunk = yMap.getValue();
				if (compressedChunk != null)
					list.add(compressedChunk);
				else
					throw new NullPointerException("value of NestedMap is null");
			}
		}
		return list;
	}

	@Override
	public boolean hasCompressedChunk(Integer chunkX, Integer chunkY) {
		return chunks_compressed.getChild(chunkX).getChild(chunkY).getValue() != null;
	}

	@Override
	public boolean removeCompressedChunkImpl(Integer chunkX, Integer chunkY) {
		
		Nestable<Integer, CompressedChunk> nestable1 = chunks_compressed.getChild(chunkX);
		if (nestable1 instanceof NestedNull)
			return false;
		
		Nestable<Integer, CompressedChunk> nestable2 = nestable1.getChild(chunkY);
		if (nestable2 instanceof NestedNull)
			return false;
		
		nestable1.removeChild(chunkY);
		if (!nestable1.hasChildren())
			chunks_compressed.removeChild(chunkX);
		
		return true;
	}

	@Override
	public void clearCompressedChunksImpl() {
		chunks_compressed.clear();
	}

	
	public void setParams(ParamContainer paramContainer) {
		this.paramContainer = paramContainer;
	}
	
	public ParamContainer getParams() {
		return this.paramContainer;
	}
	
	public ComplexNumber getAnchor() {
		return anchor;
	}
}
