package de.felixperko.fractals.data;

import java.io.IOException;
import java.io.Serializable;

import org.xerial.snappy.Snappy;

public class CompressedChunk implements Serializable{
	
	private static final long serialVersionUID = 2576995300129069335L;

	int upsample;
	int jobId;
	
	byte[] values_compressed;
	byte[] samples_compressed;
	byte[] failedSamples_compressed;
	
	int chunkX;
	int chunkY;
	int dimensionSize;
	
	public CompressedChunk(ReducedNaiveChunk chunk, int upsample, int jobId) {
		this.upsample = upsample;
		this.jobId = jobId;
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
			ReducedNaiveChunk chunk = new ReducedNaiveChunk(chunkX, chunkY, dimensionSize, getFullFloatArray(values), getFullByteArray(samples), getFullByteArray(failedSamples));
			chunk.setJobId(jobId);
			return chunk;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private float[] getFullFloatArray(float[] arr) {
		float[] ans = new float[arr.length*upsample];
		int dim = (int)Math.sqrt(arr.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim;
		for (int i = 0 ; i < arr.length ; i++) {
			ans[start] = arr[i];
			rowCounter--;
			if (rowCounter == 0) {
				start += dim*(upsample-1);
				rowCounter = dim;
			}
			start += upsample;
			if (start >= ans.length)
				break;
		}
		return ans;
	}

	private byte[] getFullByteArray(byte[] arr) {
		byte[] ans = new byte[arr.length*upsample];
		int dim = (int)Math.sqrt(arr.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim;
		for (int i = 0 ; i < arr.length ; i++) {
			ans[start] = arr[i];
			rowCounter--;
			if (rowCounter == 0) {
				start += dim*(upsample-1);
				rowCounter = dim;
			}
			start += upsample;
			if (start >= ans.length)
				break;
		}
		return ans;
	}

	private float[] getUpsampledFloatArray(float[] arr) {
		float[] ans = new float[arr.length/upsample];
		int dim = (int)Math.sqrt(arr.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim;
		for (int i = 0 ; i < ans.length ; i++) {
			ans[i] = arr[start];
			rowCounter--;
			if (rowCounter == 0) {
				start += dim*(upsample-1);
				rowCounter = dim;
			}
			start += upsample;
			if (start >= arr.length)
				break;
		}
		return ans;
	}

	private byte[] getUpsampledByteArray(byte[] arr) {
		byte[] ans = new byte[arr.length/upsample];
		int dim = (int)Math.sqrt(arr.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim;
		for (int i = 0 ; i < ans.length ; i++) {
			ans[i] = arr[start];
			rowCounter--;
			if (rowCounter == 0) {
				start += dim*(upsample-1);
				rowCounter = dim;
			}
			start += upsample;
			if (start >= arr.length)
				break;
		}
		return ans;
	}

	public int getJobId() {
		return jobId;
	}
}
