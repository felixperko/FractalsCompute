package de.felixperko.fractals.data;

public abstract class AbstractArrayChunk extends AbstractChunk {
	
	private static final long serialVersionUID = -338312489401474113L;
	int dimensionSize;
	int arrayLength;
	
	public AbstractArrayChunk(int chunkX, int chunkY, int dimensionSize) {
		super(chunkX, chunkY);
		
		this.dimensionSize = dimensionSize;
		this.arrayLength = dimensionSize*dimensionSize;		
	}

	public int getArrayLength() {
		return arrayLength;
	}

	public int getChunkDimensions() {
		return dimensionSize;
	}

	public void setDimensionSize(int dimensionSize) {
		this.dimensionSize = dimensionSize;
	}

	public void setArrayLength(int arrayLength) {
		this.arrayLength = arrayLength;
	}
	
	@Override
	public int getIndex(int chunkX, int chunkY) {
		return chunkX*dimensionSize + chunkY;
	}

}