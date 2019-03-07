package de.felixperko.fractals.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ChunkBorderDataImpl implements ChunkBorderData {
	
	private static final long serialVersionUID = 3137928899767222203L;

	AbstractArrayChunk chunk;
	
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

}
