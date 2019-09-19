package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.AbstractBFViewData;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class OrbitViewData extends AbstractBFViewData {

	@Override
	public Chunk getBufferedChunk(Integer chunkX, Integer chunkY) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Chunk> getBufferedChunks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasBufferedChunk(Integer chunkX, Integer chunkY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CompressedChunk getCompressedChunk(Integer chunkX, Integer chunkY) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CompressedChunk> getCompressedChunks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCompressedChunk(Integer chunkX, Integer chunkY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean insertBufferedChunkImpl(Chunk chunk, boolean insertCompressedChunk) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean updateBufferedChunkImpl(Chunk chunk) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean removeBufferedChunkImpl(Integer chunkX, Integer chunkY, boolean removeCompressed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void clearBufferedChunksImpl() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean insertCompressedChunkImpl(CompressedChunk compressedChunk, boolean insertBuffered) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean updateCompressedChunkImpl(CompressedChunk compressedChunk, boolean updateBuffered) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean removeCompressedChunkImpl(Integer chunkX, Integer chunkY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void clearCompressedChunksImpl() {
		// TODO Auto-generated method stub
		
	}
}
