package de.felixperko.fractals.data;

import java.util.List;

public class ChunkBorderDataImplNull implements ChunkBorderData {

	private static final long serialVersionUID = 3220272617615167154L;

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
		return null;
	}

	@Override
	public void updateDone() {
	}

	@Override
	public BorderAlignment getAlignment() {
		return null;
	}

}
