package de.felixperko.fractals.data;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public class ArrayChunkFactory implements Serializable{
	
	private static final long serialVersionUID = 2001744194301335070L;
	
	int dimensionSize;
	Class<? extends AbstractArrayChunk> chunkClass;
	
	public ArrayChunkFactory(Class<? extends AbstractArrayChunk> chunkClass, int dimensionSize) {
		this.chunkClass = chunkClass;
		this.dimensionSize = dimensionSize;
	}
	
	public AbstractArrayChunk createChunk(int chunkX, int chunkY) {
		try {
			return chunkClass.getDeclaredConstructor(int.class, int.class, int.class).newInstance(chunkX, chunkY, dimensionSize);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
//		return new NaiveChunk(chunkX, chunkY, dimensionSize);
	}

	public int getChunkSize() {
		return dimensionSize;
	}
}
