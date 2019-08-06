package de.felixperko.fractals.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ChunkBorderDataImpl implements ChunkBorderData {
	
	private static final long serialVersionUID = 3137928899767222203L;

	transient AbstractArrayChunk chunk; //TODO has to be set again if remote?
	
	BitSet bits;
	
	BorderAlignment alignment;
	
	List<Integer> changedIndices = new ArrayList<>();
	
	public ChunkBorderDataImpl(AbstractArrayChunk chunk, BorderAlignment alignment) {
		this.chunk = chunk;
		this.alignment = alignment;
		bits = new BitSet();
	}
	
	@Override
	public AbstractArrayChunk getChunk() {
		return chunk;
	}
	
	@Override
	public void setChunk(AbstractArrayChunk chunk) {
		this.chunk = chunk;
	}

//	@Override
//	public AbstractArrayChunk getBorderChunk() {
//		return null;
//	}

	@Override
	public boolean isSet(int borderIndex) {
		return bits.get(borderIndex);
	}

	@Override
	public synchronized void set(boolean set, int lowerIndex, int higherIndex) {
		if (lowerIndex < 0 || higherIndex >= chunk.getChunkDimensions())
			throw new IllegalArgumentException("set() out of bounds: "+lowerIndex+" - "+higherIndex+"  "+ (lowerIndex < 0 ? "indices can't be negative" : "indices can't be higher than the current dimension ("+chunk.getChunkDimensions())+")");
		if (lowerIndex > higherIndex)
			throw new IllegalArgumentException("Unexpected arguments: the lowerIndex ("+lowerIndex+") is higher than the higherIndex ("+higherIndex+")");
		
		for (int i = lowerIndex ; i <= higherIndex ; i++) {
			boolean prev = bits.get(i);
			if (!prev && set) {
				changedIndices.add(i);
				bits.set(i, set);
			}
		}
	}

	@Override
	public synchronized void updateDone() {
		changedIndices.clear();
	}

	@Override
	public List<Integer> getChangedIndices() {
		return changedIndices;
	}

	
	@Override
	public BorderAlignment getAlignment() {
		return alignment;
	}

	@Override
	public ChunkBorderData copy() {
		ChunkBorderDataImpl copy = new ChunkBorderDataImpl(chunk, alignment);
		copy.setBits((BitSet)bits.clone());
		return copy;
	}

	public void setBits(BitSet bits) {
		this.bits = bits;
	}

}
