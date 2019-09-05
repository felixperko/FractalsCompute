package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;
import de.felixperko.fractals.system.systems.infra.ViewData;
import de.felixperko.fractals.util.NestedMap;
import de.felixperko.fractals.util.NumberUtil;

/**
 * Provides general functionality for ViewData implementations, keeping data management to subclasses
 */
public abstract class AbstractBFViewData implements ViewData{
	
	private static final long serialVersionUID = 2638706868007870329L;

	SystemContext systemContext;
	
	boolean active = false;
	
	ComplexNumber anchor;

	double bufferTimeout = 5;
	transient NestedMap<Integer, Long> lastSeen = new NestedMap<>();
	
	public AbstractBFViewData(ComplexNumber anchor) {
		this.anchor = anchor;
	}
	
	@Override
	public ViewData setContext(SystemContext systemContext) {
		if (!(systemContext instanceof BFSystemContext))
			throw new IllegalArgumentException("AbstractBFViewData only works with BFSystemContexts right now");
		this.systemContext = systemContext;
		return this;
	}
	
	@Override
	public SystemContext getContext() {
		return systemContext;
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

	@Override
	public CompressedChunk updateBufferedAndCompressedChunk(Chunk chunk) {
		updateBufferedChunk(chunk);
		
		getViewContainer().updatedBufferedChunk(chunk, this);
		
		CompressedChunk compressedChunk = new CompressedChunk((ReducedNaiveChunk) chunk);
		updateCompressedChunk(compressedChunk, false);
		
		getViewContainer().updatedCompressedChunk(compressedChunk, this);
		
		return compressedChunk;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public void setActive(boolean active) {
		this.active = active;
	}
	
	private ViewContainer getViewContainer() {
		return systemContext.getViewContainer();
	}
	
	//buffered chunk operation delegates
	@Override
	public boolean insertBufferedChunk(Chunk chunk, boolean insertCompressedChunk) {
		boolean result = insertBufferedChunkImpl(chunk, insertCompressedChunk);
		getViewContainer().insertedBufferedChunk(chunk, this);
		return result;
	}
	
	@Override
	public boolean updateBufferedChunk(Chunk chunk) {
		boolean result = updateBufferedChunkImpl(chunk);
		getViewContainer().updatedBufferedChunk(chunk, this);
		return result;
	}
	
	@Override
	public boolean removeBufferedChunk(Integer chunkX, Integer chunkY, boolean removeCompressed) {
		boolean result = removeBufferedChunkImpl(chunkX, chunkY, removeCompressed);
		if (result)
			getViewContainer().removedBufferedChunk(chunkX, chunkY, this);
		return result;
	}
	
	@Override
	public void clearBufferedChunks() {
		clearBufferedChunksImpl();
		getViewContainer().clearedBufferedChunks(this);
	}
	
	//buffered chunk operation submethods
	abstract boolean insertBufferedChunkImpl(Chunk chunk, boolean insertCompressedChunk);
	abstract boolean updateBufferedChunkImpl(Chunk chunk);
	abstract boolean removeBufferedChunkImpl(Integer chunkX, Integer chunkY, boolean removeCompressed);
	abstract void clearBufferedChunksImpl();
	
	//compressed chunk operation delegates
	@Override
	public boolean insertCompressedChunk(CompressedChunk compressedChunk, boolean insertBuffered) {
		boolean result = insertCompressedChunkImpl(compressedChunk, insertBuffered);
		getViewContainer().insertedCompressedChunk(compressedChunk, this);
		return result;
	}
	
	@Override
	public boolean updateCompressedChunk(CompressedChunk compressedChunk, boolean updateBuffered) {
		boolean result = updateCompressedChunkImpl(compressedChunk, updateBuffered);
		getViewContainer().updatedCompressedChunk(compressedChunk, this);
		return result;
	}
	
	@Override
	public boolean removeCompressedChunk(Integer chunkX, Integer chunkY) {
		boolean result = removeCompressedChunkImpl(chunkX, chunkY);
		if (result)
			getViewContainer().removedCompressedChunk(chunkX, chunkY, this);
		return result;
	}
	
	@Override
	public void clearCompressedChunks() {
		clearCompressedChunksImpl();
		getViewContainer().clearedCompressedChunks(this);
	}

	
	//compressed chunk operation submethods
	abstract boolean insertCompressedChunkImpl(CompressedChunk compressedChunk, boolean insertBuffered);
	abstract boolean updateCompressedChunkImpl(CompressedChunk compressedChunk, boolean updateBuffered);
	abstract boolean removeCompressedChunkImpl(Integer chunkX, Integer chunkY);
	abstract void clearCompressedChunksImpl();
}
