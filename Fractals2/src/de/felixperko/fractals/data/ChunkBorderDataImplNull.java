package de.felixperko.fractals.data;

import java.util.List;

public class ChunkBorderDataImplNull implements ChunkBorderData {

	@Override
	public AbstractArrayChunk getChunk() {
		return null;
	}

	@Override
	public boolean isSet(int borderIndex) {
		return false;
	}

	@Override
	public void set(boolean set, int lowerIndex, int higherIndex) {
		throw new IllegalStateException("method call for ChunkBorderDataImplNull shouldn't be possible");
	}

	@Override
	public List<Integer> getChangedIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public BorderAlignment getAlignment() {
		// TODO Auto-generated method stub
		return null;
	}

}
