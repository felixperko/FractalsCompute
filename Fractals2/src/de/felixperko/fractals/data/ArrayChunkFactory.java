package de.felixperko.fractals.data;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.system.systems.infra.ViewData;

public class ArrayChunkFactory implements Serializable{
	
	private static final long serialVersionUID = 2001744194301335070L;
	private static final Logger LOG = LoggerFactory.getLogger(ArrayChunkFactory.class);
	
	int dimensionSize;
	Class<? extends AbstractArrayChunk> chunkClass;
	transient ViewData viewData = null;
	
	public ArrayChunkFactory(Class<? extends AbstractArrayChunk> chunkClass, int dimensionSize) {
		this.chunkClass = chunkClass;
		this.dimensionSize = dimensionSize;
	}
	
	public AbstractArrayChunk createChunk(long chunkX, long chunkY) {
		if (viewData == null) {
			LOG.warn("Exception at "+System.currentTimeMillis());
			throw new IllegalStateException("Couldn't create Chunk: ViewData is null");
		}
		try {
			return chunkClass.getDeclaredConstructor(ViewData.class, int.class, int.class, int.class).newInstance(viewData, (int)chunkX, (int)chunkY, dimensionSize);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.err.println("Failed to create instance of "+chunkClass.getName());
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof ArrayChunkFactory))
			return false;
		ArrayChunkFactory other = (ArrayChunkFactory) obj;
		if (chunkClass == null ^ other.chunkClass == null)
			return false;
		return ((chunkClass == null && other.chunkClass == null) || chunkClass.equals(other.chunkClass)) && dimensionSize == other.dimensionSize;
	}
}
