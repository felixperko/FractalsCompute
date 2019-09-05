package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewData;
import de.felixperko.fractals.util.NestedMap;
import de.felixperko.fractals.util.NumberUtil;

public abstract class AbstractBFViewData implements ViewData{
	
	private static final long serialVersionUID = 2638706868007870329L;

	//	SystemContext systemContext;
	ComplexNumber anchor;

	double bufferTimeout = 5;
	transient NestedMap<Integer, Long> lastSeen = new NestedMap<>();
	SystemContext systemContext;
	
	public AbstractBFViewData(ComplexNumber anchor) {
		this.anchor = anchor;
	}
	
	public AbstractBFViewData setContext(SystemContext systemContext) {
		if (!(systemContext instanceof BFSystemContext))
			throw new IllegalArgumentException("AbstractBFViewData only works with BFSystemContexts right now");
		this.systemContext = systemContext;
		return this;
	}

	@Override
	public void setBufferTimeout(double seconds) {
		this.bufferTimeout = seconds;
	}
	
	@Override
	public void tick() {
		Long time = System.nanoTime();
		Long timedOutTime = time+(long)(bufferTimeout/NumberUtil.NS_TO_S);
		
		for (Chunk bufferedChunk : getBufferedChunks()) {
			Integer x = bufferedChunk.getChunkX();
			Integer y = bufferedChunk.getChunkY();
			
			if (isSeeable(x, y)) { //seeable -> update time
				lastSeen.getOrMakeChild(x).getOrMakeChild(y).setValue(time);
			} else { //not seeable -> remove if timed out
				Long oldTime = lastSeen.getChild(x).getChild(y).getValue();
				if (oldTime == null || oldTime > timedOutTime)
					removeBufferedChunk(bufferedChunk, false);
			}
		}
	}
	
	private boolean isSeeable(Integer chunkX, Integer chunkY) {
		//TODO generalize AbstractBFViewData.isSeeable()
		return ((BFSystemContext)systemContext).getScreenDistance(chunkX, chunkY) <= ((BFSystemContext)systemContext).border_dispose;
	}

	@Override
	public ComplexNumber getAnchor() {
		return anchor;
	}
	
	@Override
	public boolean hasBufferedChunk(Chunk chunk) {
		return hasBufferedChunk(chunk.getChunkX(), chunk.getChunkY());
	}

	@Override
	public boolean removeBufferedChunk(Chunk chunk, boolean removeCompressed) {
		return removeBufferedChunk(chunk.getChunkX(), chunk.getChunkY(), removeCompressed);
	}
	
	@Override
	public boolean hasCompressedChunk(CompressedChunk compressedChunk) {
		return hasCompressedChunk(compressedChunk.getChunkX(), compressedChunk.getChunkY());
	}

	@Override
	public boolean removeCompressedChunk(CompressedChunk compressedChunk) {
		return removeCompressedChunk(compressedChunk.getChunkX(), compressedChunk.getChunkY());
	}
	
	@Override
	public void dispose() {
		clearBufferedChunks();
		clearCompressedChunks();
	}
	
	protected void lastSeenNow(Integer chunkX, Integer chunkY) {
		lastSeen.getOrMakeChild(chunkX).getOrMakeChild(chunkY).setValue(System.nanoTime());
	}
}
