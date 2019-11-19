package de.felixperko.fractals.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xerial.snappy.BitShuffle;
import org.xerial.snappy.Snappy;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.util.NumberUtil;

public class CompressedChunk implements Serializable{
	
	private static final long serialVersionUID = 2576995300129069335L;
	
	int upsample;
	int jobId;
	int taskId;
	
	byte[] values_compressed;
	byte[] samples_compressed;
	byte[] failedSamples_compressed;
	
	byte[] borderData_compressed;
	
	int chunkX;
	int chunkY;
	int dimensionSize;
	
	double priority;
	
	ComplexNumber chunkPos;
	
//	Map<BorderAlignment, ChunkBorderData> selfBorderData;
//	Map<BorderAlignment, ChunkBorderData> neighbourBorderData;
	
	byte[] storedIndices;
	List<ComplexNumber> storedPositions;
	byte[] storedIterations;
	
	public CompressedChunk(ReducedNaiveChunk chunk) {
		FractalsTask task = chunk.getCurrentTask();
		this.priority = 2;
		if (task != null){
			this.jobId = task.getJobId();
			this.taskId = task.getId();
			this.priority = task.getPriority()+2;
		}
		this.upsample = chunk.getUpsample();
		this.chunkX = chunk.chunkX;
		this.chunkY = chunk.chunkY;
		this.dimensionSize = chunk.dimensionSize;
		this.chunkPos = chunk.chunkPos;
		
		try {
			long t1 = System.nanoTime();
			
			int storedSize = 0;
			if (chunk.storedIterations != null)
				storedSize = chunk.storedIterations.size();
			
			int[] storedIndicesArray = new int[storedSize];
			int[] storedIterationsArray = new int[storedSize];
			
			if (storedSize > 0) {
				int counter = 0;
				for (Entry<Integer, ComplexNumber> e : chunk.storedPositions.entrySet()) {
					storedIndicesArray[counter++] = e.getKey();
					storedPositions.add(e.getValue());
				}
				counter = 0;
				for (Entry<Integer, Integer> e : chunk.storedIterations.entrySet()) {
					if (storedIndicesArray[counter] != e.getKey())
						throw new IllegalStateException("Code doesn't work as expected: map order doesn't match.");
					storedIterationsArray[counter++] = e.getValue();
				}
			}
			storedIndices = Snappy.compress(BitShuffle.shuffle(storedIndicesArray));
			storedIterations = Snappy.compress(BitShuffle.shuffle(storedIterationsArray));
			
			values_compressed = Snappy.compress(BitShuffle.shuffle(getUpsampledFloatArray(chunk.values)));
			samples_compressed = Snappy.compress(getUpsampledByteArray(chunk.samples));
			failedSamples_compressed = Snappy.compress(getUpsampledByteArray(chunk.failedSamples));
			
			//
			// BorderData
			//
			
			boolean[] borderData = new boolean[dimensionSize*8];
			int directionOffset = 0;
			int setOffset = dimensionSize*4;
			for (BorderAlignment alignment : BorderAlignment.values()){
				
				ChunkBorderData selfData = chunk.getBorderData(alignment);
				ChunkBorderData neighbourData = chunk.getBorderData(alignment);
				
				boolean useSelfData = selfData instanceof ChunkBorderDataArrayImpl;
				boolean useNeighbourData = selfData instanceof ChunkBorderDataArrayImpl;
				
				if ((!useSelfData && !(selfData instanceof ChunkBorderDataNullImpl)) || (!useNeighbourData && !(neighbourData instanceof ChunkBorderDataNullImpl)))
					throw new IllegalStateException("Unexpected ChunkBorder type");
				
				for (int i = 0 ; i < dimensionSize ; i++){
					if (useSelfData)
						borderData[directionOffset+i] = ((ChunkBorderDataArrayImpl)selfData).data[i];
					if (useNeighbourData)
						borderData[directionOffset+i+setOffset] = ((ChunkBorderDataArrayImpl)neighbourData).data[i];
				}
				
				directionOffset += dimensionSize;
			}
			
			byte[] borderData_bytes = new byte[dimensionSize];
			for (int i = 0 ; i < dimensionSize ; i++){
				borderData_bytes[i] = Byte.MIN_VALUE;
				for (int j = 0 ; j < 8 ; j++)
					if (borderData[j])
						borderData_bytes[i] += (1 << j);
			}
			
			borderData_compressed = Snappy.compress(borderData_bytes);
			
			double t = NumberUtil.getElapsedTimeInS(t1, 5);
			int size_uncompressed = ((chunk.values.length*4)+(chunk.samples.length)+(chunk.failedSamples.length))/(upsample*upsample) + dimensionSize;
			int size_compressed = values_compressed.length+samples_compressed.length+failedSamples_compressed.length + borderData_compressed.length;
			String kbString = (size_compressed/1000.0)+" kb / "+(size_uncompressed/1000.0)+" kb";
			System.out.println("saved bytes: "+values_compressed.length+"/"+(chunk.values.length*4/(upsample*upsample))+" "+
					(samples_compressed.length)+"/"+(chunk.samples.length/(upsample*upsample))+" "+
					(failedSamples_compressed.length)+"/"+(chunk.failedSamples.length/(upsample*upsample))+" "+
					kbString+" in "+t+"s");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		this.selfBorderData = new HashMap<>();
//		for (Entry<BorderAlignment, ChunkBorderData> e : chunk.getSelfBorderData().entrySet())
//			this.selfBorderData.put(e.getKey(), e.getValue().copy());
//
//		Map<BorderAlignment, ChunkBorderData> nbd = chunk.getNeighbourBorderData();
//		if (nbd != null){
//			this.neighbourBorderData = new HashMap<>();
//			for (Entry<BorderAlignment, ChunkBorderData> e : chunk.getNeighbourBorderData().entrySet())
//				this.neighbourBorderData.put(e.getKey(), e.getValue().copy());
//		}
	}
	
	public ReducedNaiveChunk decompress() {
		try {
			float[] values = BitShuffle.unshuffleFloatArray(Snappy.uncompress((values_compressed)));
			byte[] samples = Snappy.uncompress(samples_compressed);
			byte[] failedSamples = Snappy.uncompress(failedSamples_compressed);
			ReducedNaiveChunk chunk = new ReducedNaiveChunk(chunkX, chunkY, dimensionSize, getFullFloatArray(values), getFullByteArray(samples), getFullByteArray(failedSamples));
			chunk.setJobId(jobId);
			chunk.chunkPos = chunkPos;
			
			int[] storedIterationsArray = BitShuffle.unshuffleIntArray(Snappy.uncompress(storedIterations));
			if (storedIterationsArray.length > 0) {
				chunk.storedPositions = new HashMap<>();
				chunk.storedIterations = new HashMap<>();
				for (int i = 0 ; i < storedIterationsArray.length ; i++) {
					Integer index = storedIterationsArray[i];
					chunk.storedPositions.put(index, storedPositions.get(i));
					chunk.storedIterations.put(index, storedIterationsArray[i]);
				}
			}
			
			decompressBorderData(chunk);
//			if (selfBorderData != null)
//				chunk.setSelfBorderData(selfBorderData);
//			if (neighbourBorderData != null)
//				chunk.setNeighbourBorderData(neighbourBorderData);
			
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
			
			decompressBorderData(chunk);
//			if (selfBorderData != null)
//				chunk.setSelfBorderData(selfBorderData);
//			if (neighbourBorderData != null)
//				chunk.setNeighbourBorderData(neighbourBorderData);
			
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
	
	private void decompressBorderData(AbstractArrayChunk chunk){
		byte[] borderDataBytes;
		try {
			borderDataBytes = Snappy.uncompress(borderData_compressed);
		} catch (IOException e) {
			throw new IllegalStateException("Error while decompressing border data.");
		}
		
		boolean[] borderData = new boolean[borderDataBytes.length*8];
		for (int i = 0 ; i < borderDataBytes.length ; i++){
			int correctedByte = borderDataBytes[i]-(int)Byte.MIN_VALUE;
			int compare = 255;
			int step = 128;
			for (int j = 7 ; j >= 0 ; j--) {
				if (correctedByte == compare) {
					correctedByte -= step;
				}
				compare -= step;
				step >>= 1;
				borderData[i*8+j] = true;
			}
//			int compare = 127;
//			int step = 128;
//			byte value = borderDataBytes[i];
//			for (int j = 7 ; j >= 0 ; j--){
//				if (compare == value){
//					compare -= step;
//					value -= step;
//					step >>= 1;
//					borderData[i*8+j] = true;
//				}
//				
////				int bitMask = 1 >> j;
////				if ((correctedByte & bitMask) > 0)
////					borderData[i*8+j] = true;
//			}
		}
		
		Map<BorderAlignment, ChunkBorderData> selfBorderData = new HashMap<>();
		Map<BorderAlignment, ChunkBorderData> neighbourBorderData = new HashMap<>();
		
		int dim = chunk.dimensionSize;
		int loopCounter = 0;
		for (BorderAlignment alignment : BorderAlignment.values()){
			ChunkBorderData selfData = new ChunkBorderDataArrayImpl(chunk, alignment);
			ChunkBorderData neighbourData = new ChunkBorderDataArrayImpl(chunk, alignment);
			for (int i = 0 ; i < dim ; i++){
				if (borderData[i+loopCounter*dim])
					selfData.set(true, i);
				if (borderData[i+(loopCounter+4)*dim])
					neighbourData.set(true, i);
			}
			selfBorderData.put(alignment, selfData);
			neighbourBorderData.put(alignment, neighbourData);
			loopCounter++;
		}
		
		chunk.setSelfBorderData(selfBorderData);
		chunk.setNeighbourBorderData(neighbourBorderData);
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

	public int getTaskId() {
		return taskId;
	}

	public int getJobId() {
		return jobId;
	}
	
	public int getUpsample() {
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

//	public Map<BorderAlignment, ChunkBorderData> getSelfBorderData() {
//		return selfBorderData;
//	}
//
//	public Map<BorderAlignment, ChunkBorderData> getNeighbourBorderData() {
//		return neighbourBorderData;
//	}
	
	public double getPriority() {
		return priority;
	}
}
