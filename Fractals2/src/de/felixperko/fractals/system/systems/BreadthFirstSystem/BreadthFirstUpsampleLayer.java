package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.felixperko.fractals.util.serialization.jackson.JsonObjectDeserializer;

@JsonTypeName("bfUpsampleLayer")
@JsonDeserialize(as = BreadthFirstUpsampleLayer.class)
public class BreadthFirstUpsampleLayer extends BreadthFirstLayer{
	
	
	private static final long serialVersionUID = -6818101684225388444L;
	public static final String TYPE_NAME = "bfUpsampleLayer";
	
	int chunkSize;
	int upsample;

	public BreadthFirstUpsampleLayer(int upsample, int chunkSize) {
		super(TYPE_NAME);
		this.chunkSize = chunkSize;
		setUpsample(upsample);
	}
	
	public BreadthFirstUpsampleLayer() {
		super(TYPE_NAME);
		this.chunkSize = -1;
		this.upsample = -1;
	}
	
//	@Override
//	public GlobalPixel getNeighbour(int pixel, int chunkSize, int shiftX, int shiftY) {
//		int x = pixel / chunkSize + shiftX*upsample;
//		int y = pixel % chunkSize + shiftY*upsample;
//		if (x < 0) {
//			if (y < 0)
//				return new GlobalPixel(-1, -1, chunkSize+x, chunkSize+y);
//			return new GlobalPixel(-1, 0, chunkSize+x, y);
//		}
//		if (y < 0)
//			return new GlobalPixel(0, -1, x, chunkSize+y);
//		return new GlobalPixel(0, 0, x, y);
//	}
	
//	@Override
//	public boolean equals(Object other) {
//		if (other == null)
//			return false;
//		if (!(other instanceof BreadthFirstUpsampleLayer))
//			return false;
//		BreadthFirstUpsampleLayer o = (BreadthFirstUpsampleLayer) other;
//		return (o.id == id && o.upsample == upsample);
//	}
	
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}

	public int getUpsample() {
		return upsample;
	}
	
	public void setUpsample(int upsample) {
		
		if (this.chunkSize == -1)
			throw new IllegalStateException("chunkSize is not set");
		
		this.upsample = upsample;

		BitSet bitSet = new BitSet();
		for (int x = upsample/2 ; x < chunkSize ; x += upsample) {
			for (int y = upsample/2 ; y < chunkSize ; y += upsample) {
				bitSet.set(x*chunkSize + y);
			}
		}
		with_enabled_pixels(bitSet);
	}

	public BitSet getEnabledPixels() {
		return enabledPixels;
	}

	public int getUpsampledIndex(int i, int chunkSize) {
		int x = ((i/chunkSize)/upsample)*upsample;
		int y = ((i%chunkSize)/upsample)*upsample;
		return x*chunkSize + y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + upsample;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreadthFirstUpsampleLayer other = (BreadthFirstUpsampleLayer) obj;
		if (upsample != other.upsample)
			return false;
		return true;
	}
}
