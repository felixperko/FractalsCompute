package de.felixperko.fractals.data;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.ViewData;

import static de.felixperko.fractals.data.BorderAlignment.*;

public abstract class AbstractArrayChunk extends AbstractChunk {
	
	public static float FLAG_CULL = -2;
	private static final long serialVersionUID = -338312489401474113L;
	
	int dimensionSize;
	int arrayLength;
	
	Map<BorderAlignment, ChunkBorderData> selfBorderData = new HashMap<>();
	Map<BorderAlignment, ChunkBorderData> neighbourBorderData = null;
	
	public AbstractArrayChunk(ViewData viewData, int chunkX, int chunkY, int dimensionSize) {
		super(viewData, chunkX, chunkY);
		
		this.dimensionSize = dimensionSize;
		this.arrayLength = dimensionSize*dimensionSize;
		
		for (BorderAlignment alignment : BorderAlignment.values()) {
			selfBorderData.put(alignment, new ChunkBorderDataImpl(this, alignment));
		}
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
	
	public ChunkBorderData getBorderData(BorderAlignment alignment) {
		return selfBorderData.get(alignment);
	}
	
	@Override
	public int getIndex(int chunkX, int chunkY) {
		return chunkX*dimensionSize + chunkY;
	}
	
	private static ChunkBorderData[] emptyBorderData = new ChunkBorderData[]{};
	
	public ChunkBorderData[] getIndexBorderData(int x, int y, int upsample) {
		
		int lowestX = x-upsample-1;
		int highestX = x+upsample;
		int lowestY = y-upsample-1;
		int highestY = y+upsample;
		final int higherBorder = dimensionSize-1;
		
		//most cases -> check first
		if (lowestX > 0 && highestX < higherBorder && lowestY > 0 && highestY < higherBorder)
			return emptyBorderData;
		
		if (lowestX <= 0) {
			if (highestX >= higherBorder) {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(LEFT, RIGHT, UP, DOWN);
					else
						return getBorderDataForAlignments(LEFT, RIGHT, UP);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(LEFT, RIGHT, DOWN);
					else
						return getBorderDataForAlignments(LEFT, RIGHT);
				}
			} else {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(LEFT, UP, DOWN);
					else
						return getBorderDataForAlignments(LEFT, UP);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(LEFT, DOWN);
					else
						return getBorderDataForAlignments(LEFT);
				}
			}
		} else {
			if (highestX >= higherBorder) {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(RIGHT, UP, DOWN);
					else
						return getBorderDataForAlignments(RIGHT, UP);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(RIGHT, DOWN);
					else
						return getBorderDataForAlignments(RIGHT);
				}
			} else {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(UP, DOWN);
					else
						return getBorderDataForAlignments(UP);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(DOWN);
					else
						throw new IllegalStateException("unexpected branch in AbstractArrayChunk.getIndexBorderData()");
				}
			}
		}
	}

	private ChunkBorderData[] getBorderDataForAlignments(BorderAlignment... alignments) {
		ChunkBorderData[] res = new ChunkBorderData[alignments.length];
		for (int i = 0 ; i < alignments.length ; i++) {
			res[i] = selfBorderData.get(alignments[i]);
		}
		return res;
	}
	
	public void setNeighbourBorderData(Map<BorderAlignment, ChunkBorderData> neighbourBorderData) {
		this.neighbourBorderData = neighbourBorderData;
	}
	
	public ChunkBorderData getNeighbourBorderData(BorderAlignment alignment) {
		return neighbourBorderData.get(alignment);
	}

	
	public void setCullFlags(int upsampleIndex, int upsample, boolean cull) {
		int startX = (upsampleIndex / dimensionSize);
		int startY = (upsampleIndex % dimensionSize);
		startX -= startX%upsample;
		startY -= startY%upsample;
		int offX = startX*dimensionSize;
		for (int x = startX ; x < upsample+startX ; x++) {
			for (int y = startY ; y < upsample+startY ; y++) {
				int i = offX + y;
				setCullFlag(i, cull, upsample);
			}
			offX += dimensionSize;
		}
	}

	private void setCullFlag(int i, boolean cull, int upsample) {
		if (cull)
			addSample(i, FLAG_CULL, upsample);
		else {
			if (getValue(i, true) == FLAG_CULL)
				removeFlag(i);
		}
	}

	protected abstract void removeFlag(int i);
}