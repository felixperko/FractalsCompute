package de.felixperko.fractals.data;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.ViewData;

public class ArrayChunkFactory implements Serializable{
	
	private static final long serialVersionUID = 2001744194301335070L;
	
	int dimensionSize;
	Class<? extends AbstractArrayChunk> chunkClass;
	transient ViewData viewData = null;
	
	public ArrayChunkFactory(Class<? extends AbstractArrayChunk> chunkClass, int dimensionSize) {
		this.chunkClass = chunkClass;
		this.dimensionSize = dimensionSize;
	}
	
	public AbstractArrayChunk createChunk(int chunkX, int chunkY) {
		if (viewData == null)
			throw new IllegalStateException("Couldn't create Chunk: ViewData is null");
		try {
			return chunkClass.getDeclaredConstructor(ViewData.class, int.class, int.class, int.class).newInstance(viewData, chunkX, chunkY, dimensionSize);
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
	
	public void setViewData(ViewData viewData) {
		this.viewData = viewData;
	}
}
