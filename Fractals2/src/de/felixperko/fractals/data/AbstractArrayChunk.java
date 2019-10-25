package de.felixperko.fractals.data;

import static de.felixperko.fractals.data.BorderAlignment.DOWN;
import static de.felixperko.fractals.data.BorderAlignment.LEFT;
import static de.felixperko.fractals.data.BorderAlignment.RIGHT;
import static de.felixperko.fractals.data.BorderAlignment.UP;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.ViewData;

public abstract class AbstractArrayChunk extends AbstractChunk {
	
	public static float FLAG_CULL = -2;
	private static final long serialVersionUID = -338312489401474113L;
	
	int dimensionSize;
	int arrayLength;
	int upsample;
	
	Map<BorderAlignment, ChunkBorderData> selfBorderData = new HashMap<>();
	Map<BorderAlignment, ChunkBorderData> neighbourBorderData = null;
	
	Map<Integer, ComplexNumber> storedPositions;
	Map<Integer, Integer> storedIterations;
	
	public AbstractArrayChunk(ViewData viewData, int chunkX, int chunkY, int dimensionSize) {
		super(viewData, chunkX, chunkY);
		
		this.dimensionSize = dimensionSize;
		this.arrayLength = dimensionSize*dimensionSize;
		this.upsample = 1;
		
		for (BorderAlignment alignment : BorderAlignment.values()) {
			selfBorderData.put(alignment, new ChunkBorderDataArrayImpl(this, alignment));
		}
	}

	public int getChunkDimensions() {
		return dimensionSize;
	}

	public void setDimensionSize(int dimensionSize) {
		this.dimensionSize = dimensionSize;
	}

	public int getArrayLength() {
		return arrayLength;
	}

	public void setArrayLength(int arrayLength) {
		this.arrayLength = arrayLength;
	}

	public int getUpsample(){
		return upsample;
	}
	
	public void setUpsample(int upsample){
		this.upsample = upsample;
	}
	
	public ChunkBorderData getBorderData(BorderAlignment alignment) {
		return selfBorderData.get(alignment);
	}
	
	@Override
	public int getIndex(int chunkX, int chunkY) {
		return chunkX*dimensionSize + chunkY;
	}
	
	private static ChunkBorderData[] emptyBorderData = new ChunkBorderData[]{};
	private static ChunkBorderDataNullImpl nullBorderData = new ChunkBorderDataNullImpl();
	
	public ChunkBorderData[] getIndexBorderData(int x, int y, int upsample) {
		
		int lowestX = x-upsample/2-1;
		int highestX = lowestX+upsample/2;
		int lowestY = y-upsample/2-1;
		int highestY = lowestY+upsample;
		final int higherBorder = dimensionSize-1;
		
		//most cases -> check first
		if (lowestX > 0 && highestX < higherBorder && lowestY > 0 && highestY < higherBorder)
			return emptyBorderData;
		
		//DOWN -> RIGHT
		//UP -> LEFT
		//RIGHT -> DOWN
		//LEFT -> UP
		
//		if (lowestY <= 0) {
//			if (highestY >= higherBorder) {
//				if (lowestX <= 0) {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(UP, DOWN, LEFT, RIGHT);
//					else
//						return getBorderDataForAlignments(UP, DOWN, LEFT);
//				} else {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(UP, DOWN, RIGHT);
//					else
//						return getBorderDataForAlignments(UP, DOWN);
//				}
//			} else {
//				if (lowestX <= 0) {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(UP, LEFT, RIGHT);
//					else
//						return getBorderDataForAlignments(UP, LEFT);
//				} else {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(UP, RIGHT);
//					else
//						return getBorderDataForAlignments(UP);
//				}
//			}
//		} else {
//			if (highestY >= higherBorder) {
//				if (lowestX <= 0) {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(DOWN, LEFT, RIGHT);
//					else
//						return getBorderDataForAlignments(DOWN, LEFT);
//				} else {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(DOWN, RIGHT);
//					else
//						return getBorderDataForAlignments(DOWN);
//				}
//			} else {
//				if (lowestX <= 0) {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(LEFT, RIGHT);
//					else
//						return getBorderDataForAlignments(LEFT);
//				} else {
//					if (highestX >= higherBorder)
//						return getBorderDataForAlignments(RIGHT);
//					else
//						throw new IllegalStateException("unexpected branch in AbstractArrayChunk.getIndexBorderData()");
//				}
//			}
//		}
		
		if (lowestX <= 0) {
			if (highestX >= higherBorder) {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(UP, DOWN, LEFT, RIGHT);
					else
						return getBorderDataForAlignments(UP, DOWN, LEFT);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(UP, DOWN, RIGHT);
					else
						return getBorderDataForAlignments(UP, DOWN);
				}
			} else {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(UP, LEFT, RIGHT);
					else
						return getBorderDataForAlignments(UP, LEFT);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(UP, RIGHT);
					else
						return getBorderDataForAlignments(UP);
				}
			}
		} else {
			if (highestX >= higherBorder) {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(DOWN, LEFT, RIGHT);
					else
						return getBorderDataForAlignments(DOWN, LEFT);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(DOWN, RIGHT);
					else
						return getBorderDataForAlignments(DOWN);
				}
			} else {
				if (lowestY <= 0) {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(LEFT, RIGHT);
					else
						return getBorderDataForAlignments(LEFT);
				} else {
					if (highestY >= higherBorder)
						return getBorderDataForAlignments(RIGHT);
					else
						throw new IllegalStateException("unexpected branch in AbstractArrayChunk.getIndexBorderData()");
				}
			}
		}
	}

	private ChunkBorderData[] getBorderDataForAlignments(BorderAlignment... alignments) {
		ChunkBorderData[] res = new ChunkBorderData[alignments.length];
		for (int i = 0 ; i < alignments.length ; i++) {
			BorderAlignment alignment = alignments[i];
			//if (alignment.isHorizontal())
//			alignment = alignment.getAlignmentForNeighbour();
			res[i] = selfBorderData.get(alignment);
		}
		return res;
	}
	
	public void setNeighbourBorderData(Map<BorderAlignment, ChunkBorderData> neighbourBorderData) {
		this.neighbourBorderData = neighbourBorderData;
		for (ChunkBorderData data : neighbourBorderData.values())
			if (!(data instanceof ChunkBorderDataNullImpl))
				data.setChunk(this);
	}
	
	public ChunkBorderData getNeighbourBorderData(BorderAlignment alignment) {
		return neighbourBorderData.get(alignment);
	}
	
	public Map<BorderAlignment, ChunkBorderData> getNeighbourBorderData() {
		return neighbourBorderData;
	}
	
	public Map<BorderAlignment, ChunkBorderData> getSelfBorderData() {
		return selfBorderData;
	}

	public void setSelfBorderData(Map<BorderAlignment, ChunkBorderData> selfBorderData) {
		this.selfBorderData = selfBorderData;
		for (ChunkBorderData data : selfBorderData.values())
			if (!(data instanceof ChunkBorderDataNullImpl))
				data.setChunk(this);
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
				setCullFlag(i, cull, 1);
			}
			offX += dimensionSize;
		}
	}

	private void setCullFlag(int i, boolean cull, int upsample) {
		synchronized (selfBorderData) {
			if (cull)
				addSample(i, FLAG_CULL, upsample);
			else {
				if (getValue(i, true) == FLAG_CULL)
					removeFlag(i);
			}
		}
	}

	protected abstract void removeFlag(int i);

	public abstract int getStartIndex();

	public void storeCurrentState(Integer pixel, ComplexNumber current, Integer iterations) {
		storedPositions.put(pixel, current);
		storedIterations.put(pixel, iterations);
	}

	public ComplexNumber getStoredPosition(Integer pixel) {
		if (storedPositions == null)
			return null;
		return storedPositions.get((Integer)pixel);
	}

	public Integer getStoredIterations(Integer pixel) {
		if (storedIterations == null)
			return null;
		return storedIterations.get(pixel);
	}
	
	public void removeStoredPosition(Integer pixel) {
		storedPositions.remove(pixel);
		storedIterations.remove(pixel);
	}
}