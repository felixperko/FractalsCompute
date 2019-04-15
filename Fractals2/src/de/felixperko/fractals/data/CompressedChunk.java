package de.felixperko.fractals.data;

import java.io.IOException;
import java.io.Serializable;

import org.xerial.snappy.Snappy;

public class CompressedChunk implements Serializable{
	
	private static final long serialVersionUID = 2576995300129069335L;

	int upsample;
	
	byte[] values_compressed;
	byte[] samples_compressed;
	byte[] failedSamples_compressed;
	
	int chunkX;
	int chunkY;
	int dimensionSize;
	
	public CompressedChunk(ReducedNaiveChunk chunk, int upsample) {
		this.upsample = upsample;
		this.chunkX = chunk.chunkX;
		this.chunkY = chunk.chunkY;
		this.dimensionSize = chunk.dimensionSize;
		try {
			values_compressed = Snappy.compress(getUpsampledFloatArray(chunk.values));
			samples_compressed = Snappy.compress(getUpsampledByteArray(chunk.samples));
			failedSamples_compressed = Snappy.compress(getUpsampledByteArray(chunk.failedSamples));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ReducedNaiveChunk decompress() {
		try {
			float[] values = Snappy.uncompressFloatArray(values_compressed);
			byte[] samples = Snappy.uncompress(samples_compressed);
			byte[] failedSamples = Snappy.uncompress(failedSamples_compressed);
			return new ReducedNaiveChunk(chunkX, chunkY, dimensionSize, values, samples, failedSamples);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private float[] getUpsampledFloatArray(float[] arr) {
		float[] ans = new float[arr.length/upsample];
		int start = (upsample/2)*arr.length + upsample/2;
		for (int i = 0 ; i < ans.length ; i++) {
			ans[i] = arr[start];
			start += upsample*upsample;
		}
		return ans;
	}

	private byte[] getUpsampledByteArray(byte[] arr) {
		byte[] ans = new byte[arr.length/upsample];
		int start = (upsample/2)*arr.length + upsample/2;
		for (int i = 0 ; i < ans.length ; i++) {
			ans[i] = arr[start];
			start += upsample*upsample;
		}
		return ans;
	}
}
