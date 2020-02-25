package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;

public class UpsampleEnabledPixels implements EnabledPixels {
	
	public static void main(String[] args) {
		
		int chunkSize = 8;
		int upsample = 4;
		
		UpsampleEnabledPixels ep = new UpsampleEnabledPixels(8, 4);
		int i = 0;
		while (i != -1){
			i = ep.nextEnabled(i);
			int x = i/ep.chunkSize;
			int y = i%ep.chunkSize;
			System.out.println(x+","+y+" ("+i+")");
		}
		
		BitSet bitSet = new BitSet();
		for (int x = upsample/2 ; x < chunkSize ; x += upsample) {
			for (int y = upsample/2 ; y < chunkSize ; y += upsample) {
				bitSet.set(x*chunkSize + y);
				System.out.println(x*chunkSize + y);
			}
		}
		
//		for (int i2 = bitSet.nextSetBit(0) ; i2 != -1 ; i2 = bitSet.nextSetBit(i2)){
//			System.out.println(i2);
//		}
	}
	
	int chunkSize;
	int upsample;
	
	public UpsampleEnabledPixels(int chunkSize, int upsample) {
		if (chunkSize <= 0)
			throw new IllegalStateException("chunkSize is invalid");
		setChunkSize(chunkSize);
		setUpsample(upsample);
	}
	
	@Override
	public boolean isEnabled(int pixel) {
		int x = pixel/chunkSize;
		int y = pixel%chunkSize;
		return x % upsample == upsample/2 && y % upsample == upsample/2;
	}

	@Override
	public int nextEnabled(int currentPixel) {
		
//		if (isEnabled(currentPixel))
//			currentPixel++;
//		while (!isEnabled(currentPixel))
//			currentPixel++;
//		if (currentPixel > chunkSize*chunkSize)
//			currentPixel = -1;
//		return currentPixel;
		
		int x = currentPixel/chunkSize;
		int y = currentPixel%chunkSize;
		
		int xBlock = x/upsample;
		int yBlock = y/upsample;
		
		int enabled = (xBlock * upsample + upsample/2)*chunkSize + yBlock * upsample + upsample/2;
		
		if (enabled <= currentPixel){
			yBlock++;
			if (yBlock >= chunkSize/upsample){
				xBlock++;
				yBlock = 0;
			}
			enabled = (xBlock * upsample + upsample/2)*chunkSize + yBlock * upsample + upsample/2;
		}
		
		if (enabled < chunkSize*chunkSize)
			return enabled;
		return -1;
		
//		
//		int xOffset = x % upsample;
//		int yOffset = y % upsample;
//		
//		int add = 0;
//		
//		if (xOffset < upsample/2)
//			add += (upsample/2 - xOffset)*chunkSize;
//		else if (xOffset > upsample/2)
//			add += (upsample-xOffset + upsample/2)*chunkSize;
//		
//		if (yOffset < upsample/2)
//			add += (upsample/2 - yOffset);
//		else if (yOffset > upsample/2)
//			add += (upsample-yOffset + upsample/2);
//		
//		if (xOffset == 0 && yOffset == 0)
//			add += upsample*upsample;
//		
//		int res = currentPixel+add;
//		if (res < chunkSize*chunkSize)
//			return res;
//		else
//			return -1;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public int getUpsample() {
		return upsample;
	}

	public void setUpsample(int upsample) {
		this.upsample = upsample;
	}

}
