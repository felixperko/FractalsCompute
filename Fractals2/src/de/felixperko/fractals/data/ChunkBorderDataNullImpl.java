package de.felixperko.fractals.data;

import java.util.List;

public class ChunkBorderDataNullImpl implements ChunkBorderData {

	private static final long serialVersionUID = 3220272617615167154L;

	@Override
	public AbstractArrayChunk getChunk() {
		return null;
	}
	
	@Override
	public void setChunk(AbstractArrayChunk chunk) {
		throw new IllegalStateException("setChunk() shouldn't be called for null implementation");
	}

	@Override
	public boolean isSet(int borderIndex) {
		return false;
	}

	@Override
	public void set(boolean set, int index) {
		throw new IllegalStateException("method call for ChunkBorderDataImplNull shouldn't be possible");
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

	@Override
	public ChunkBorderData copy() {
		return new ChunkBorderDataNullImpl();
	}

}
