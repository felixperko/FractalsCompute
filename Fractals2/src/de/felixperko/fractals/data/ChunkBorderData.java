package de.felixperko.fractals.data;

import java.io.Serializable;
import java.util.List;

public interface ChunkBorderData extends Serializable{

	public AbstractArrayChunk getChunk();
//	public AbstractArrayChunk getBorderChunk();
	
	public boolean isSet(int borderIndex);
	public void set(boolean set, int lowerIndex, int higherIndex);
	
	public List<Integer> getChangedIndices();
	public void updateDone();
	
	public BorderAlignment getAlignment();
	
	public ChunkBorderData copy();
}
