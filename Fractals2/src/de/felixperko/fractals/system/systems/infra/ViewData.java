package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;
import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public interface ViewData extends Serializable{
	
	boolean insertBufferedChunk(Chunk chunk);
	boolean updateBufferedChunk(Chunk chunk);
	Chunk getBufferedChunk(Integer chunkX, Integer chunkY);
	List<Chunk> getBufferedChunks();
	boolean hasBufferedChunk(Integer chunkX, Integer chunkY);
	boolean hasBufferedChunk(Chunk chunk);
	boolean removeBufferedChunk(Integer chunkX, Integer chunkY, boolean removeCompressed);
	boolean removeBufferedChunk(Chunk chunk, boolean removeCompressed);
	void clearBufferedChunks();
	
	void setBufferTimeout(double seconds);
	void tick();
	
	boolean insertCompressedChunk(CompressedChunk compressedChunk, boolean insertBuffered);
	boolean updateCompressedChunk(CompressedChunk compressedChunk, boolean updateBuffered);
	CompressedChunk getCompressedChunk(Integer chunkX, Integer chunkY);
	List<CompressedChunk> getCompressedChunks();
	boolean hasCompressedChunk(Integer chunkX, Integer chunkY);
	boolean hasCompressedChunk(CompressedChunk compressedChunk);
	boolean removeCompressedChunk(Integer chunkX, Integer chunkY);
	boolean removeCompressedChunk(CompressedChunk compressedChunk);
	void clearCompressedChunks();
	
	ComplexNumber getAnchor();

	void dispose();

}