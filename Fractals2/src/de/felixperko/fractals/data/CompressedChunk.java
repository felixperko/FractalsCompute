package de.felixperko.fractals.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.xerial.snappy.BitShuffle;
import org.xerial.snappy.Snappy;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.util.NumberUtil;

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
	
	ComplexNumber chunkPos;
	
	Map<BorderAlignment, ChunkBorderData> selfBorderData;
	Map<BorderAlignment, ChunkBorderData> neighbourBorderData;
	
	public CompressedChunk(ReducedNaiveChunk chunk, int upsample, int jobId, boolean includeBorderData) {
		this.upsample = upsample;
		this.jobId = jobId;
		this.chunkX = chunk.chunkX;
		this.chunkY = chunk.chunkY;
		this.dimensionSize = chunk.dimensionSize;
		this.chunkPos = chunk.chunkPos;
		try {
			long t1 = System.nanoTime();
			values_compressed = Snappy.compress(BitShuffle.shuffle(getUpsampledFloatArray(chunk.values)));
			samples_compressed = Snappy.compress(getUpsampledByteArray(chunk.samples));
			failedSamples_compressed = Snappy.compress(getUpsampledByteArray(chunk.failedSamples));
			double t = NumberUtil.getElapsedTimeInS(t1, 5);
			int size_uncompressed = ((chunk.values.length*4)+(chunk.samples.length)+(chunk.failedSamples.length))/(upsample*upsample);
			int size_compressed = values_compressed.length+samples_compressed.length+failedSamples_compressed.length;
			String kbString = (size_compressed/1000.0)+" kb / "+(size_uncompressed/1000.0)+" kb";
			System.out.println("saved bytes: "+values_compressed.length+"/"+(chunk.values.length*4/(upsample*upsample))+" "+
					(samples_compressed.length)+"/"+(chunk.samples.length/(upsample*upsample))+" "+
					(failedSamples_compressed.length)+"/"+(chunk.failedSamples.length/(upsample*upsample))+" "+
					kbString+" in "+t+"s");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (includeBorderData) {
			this.selfBorderData = chunk.getSelfBorderData();
			this.neighbourBorderData = chunk.getNeighbourBorderData();
		}
	}
	
	public ReducedNaiveChunk decompress() {
		try {
			float[] values = BitShuffle.unshuffleFloatArray(Snappy.uncompress((values_compressed)));
			byte[] samples = Snappy.uncompress(samples_compressed);
			byte[] failedSamples = Snappy.uncompress(failedSamples_compressed);
			ReducedNaiveChunk chunk = new ReducedNaiveChunk(chunkX, chunkY, dimensionSize, getFullFloatArray(values), getFullByteArray(samples), getFullByteArray(failedSamples));
			chunk.setJobId(jobId);
			chunk.chunkPos = chunkPos;
			if (selfBorderData != null)
				chunk.setSelfBorderData(selfBorderData);
			if (neighbourBorderData != null)
				chunk.setNeighbourBorderData(neighbourBorderData);
			return chunk;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public AbstractArrayChunk decompressPacked() {
		try {
			float[] values = BitShuffle.unshuffleFloatArray(Snappy.uncompress((values_compressed)));
			byte[] samples = Snappy.uncompress(samples_compressed);
			byte[] failedSamples = Snappy.uncompress(failedSamples_compressed);
			AbstractArrayChunk chunk = new ReducedNaivePackedChunk(chunkX, chunkY, dimensionSize, values, samples, failedSamples, upsample);
			chunk.setJobId(jobId);
			chunk.chunkPos = chunkPos;
			if (selfBorderData != null)
				chunk.setSelfBorderData(selfBorderData);
			if (neighbourBorderData != null)
				chunk.setNeighbourBorderData(neighbourBorderData);
			return chunk;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private float[] getFullFloatArray(float[] arr) {
		if (upsample == 1)
			return arr;
		float[] ans = new float[arr.length*upsample*upsample];
		int upsampleDim = (int)Math.sqrt(arr.length);
		int dim = (int)Math.sqrt(ans.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim/upsample;
		for (int i = 0 ; i < arr.length ; i++) {
			int x = (i / upsampleDim)*upsample;
			int y = (i % upsampleDim)*upsample;
//			System.out.println(x+"/"+y);
			int startX = x;
			int startY = y;
			float value = arr[i];
//			System.out.println("up: "+upsample+" "+startX+"/"+startY+" "+value);
			for (int x2 = startX ; x2 < startX+upsample ; x2++) {
				for (int y2 = startY ; y2 < startY+upsample ; y2++) {
					int index = x2*dim + y2;
					ans[index] = value;
				}
			}
//			ans[start] = arr[i];
//			rowCounter--;
//			if (rowCounter == 0) {
//				start += dim*(upsample-1);
//				rowCounter = dim/upsample;
//			}
//			start += upsample;
//			if (start >= ans.length)
//				break;
		}
		return ans;
	}

	private byte[] getFullByteArray(byte[] arr) {
		if (upsample == 1)
			return arr;
		byte[] ans = new byte[arr.length*upsample*upsample];
		int upsampleDim = (int)Math.sqrt(arr.length);
		int dim = (int)Math.sqrt(ans.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim/upsample;
		for (int i = 0 ; i < arr.length ; i++) {
			int x = (i / upsampleDim)*upsample;
			int y = (i % upsampleDim)*upsample;
			int startX = x;
			int startY = y;
			byte value = arr[i];
			for (int x2 = startX ; x2 < startX+upsample ; x2++) {
				for (int y2 = startY ; y2 < startY+upsample ; y2++) {
					int index = x2*dim + y2;
					ans[index] = value;
				}
			}
//			ans[start] = arr[i];
//			rowCounter--;
//			if (rowCounter == 0) {
//				start += dim*(upsample-1);
//				rowCounter = dim/upsample;
//			}
//			start += upsample;
//			if (start >= ans.length)
//				break;
		}
		return ans;
	}

	private float[] getUpsampledFloatArray(float[] arr) {
		if (upsample == 1)
			return arr;
		float[] ans = new float[arr.length/(upsample*upsample)];
		int dim = (int)Math.sqrt(arr.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim/upsample;
//		int[] indices = new int[arr.length];
		for (int i = 0 ; i < ans.length ; i++) {
			ans[i] = arr[start];
//			indices[start] = 1;
			rowCounter--;
			if (rowCounter == 0) {
				start += dim*(upsample-1);
				rowCounter = dim/upsample;
			}
			start += upsample;
			if (start >= arr.length)
				break;
		}
//		System.out.println();
//		for (int x = 0 ; x < dim ; x++) {
//			for (int y = 0 ; y < dim ; y++)
//				System.out.print(indices[x*dim+y]+"/"+chunk.values[x*dim+y]+" ");
//			System.out.println();
//		}
		return ans;
	}

	private byte[] getUpsampledByteArray(byte[] arr) {
		if (upsample == 1)
			return arr;
		byte[] ans = new byte[arr.length/(upsample*upsample)];
		int dim = (int)Math.sqrt(arr.length);
		int start = (upsample/2)*dim + upsample/2;
		int rowCounter = dim/upsample;
		for (int i = 0 ; i < ans.length ; i++) {
			ans[i] = arr[start];
			rowCounter--;
			if (rowCounter == 0) {
				start += dim*(upsample-1);
				rowCounter = dim/upsample;
			}
			start += upsample;
			if (start >= arr.length)
				break;
		}
		return ans;
	}

	public int getJobId() {
		return jobId;
	}	public int getUpsample() {
		return upsample;
	}

	public byte[] getValues_compressed() {
		return values_compressed;
	}

	public byte[] getSamples_compressed() {
		return samples_compressed;
	}

	public byte[] getFailedSamples_compressed() {
		return failedSamples_compressed;
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkY() {
		return chunkY;
	}

	public int getDimensionSize() {
		return dimensionSize;
	}

	public ComplexNumber getChunkPos() {
		return chunkPos;
	}

	public Map<BorderAlignment, ChunkBorderData> getSelfBorderData() {
		return selfBorderData;
	}

	public Map<BorderAlignment, ChunkBorderData> getNeighbourBorderData() {
		return neighbourBorderData;
	}
}
