package de.felixperko.fractals.data;

import java.util.Arrays;
import java.util.List;

public class ChunkBorderDataArrayImpl implements ChunkBorderData{
	
	private static final long serialVersionUID = -3633841294247697568L;
	
	AbstractArrayChunk chunk;
	BorderAlignment alignment;
	boolean[] data;
	
	public ChunkBorderDataArrayImpl(AbstractArrayChunk chunk, BorderAlignment alignment) {
		this.chunk = chunk;
		this.alignment = alignment;
		data = new boolean[chunk.dimensionSize];
	}

	@Override
	public AbstractArrayChunk getChunk() {
		return chunk;
	}

	@Override
	public void setChunk(AbstractArrayChunk chunk) {
		this.chunk = chunk;
	}

	@Override
	public boolean isSet(int borderIndex) {
		return data[borderIndex];
	}

	@Override
	public void set(boolean set, int lowerIndex, int higherIndex) {
		for (int i = lowerIndex ; i <= higherIndex ; i++){
			data[i] = set;
		}
	}

	@Override
	public List<Integer> getChangedIndices() {
		return null;
	}

	@Override
	public void updateDone() {
		
	}

	@Override
	public BorderAlignment getAlignment() {
		return alignment;
	}

	@Override
	public ChunkBorderData copy() {
		ChunkBorderDataArrayImpl copy = new ChunkBorderDataArrayImpl(chunk, alignment);
		copy.data = Arrays.copyOf(data, data.length);
		return copy;
	}

}
