package de.felixperko.fractals.data;

import java.awt.image.SampleModel;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import org.xerial.snappy.Snappy;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstViewData;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.ViewData;

public class ReducedNaiveChunk extends AbstractArrayChunk {
	
	private static final long serialVersionUID = -8824365910389869969L;
	
	float[] values;
	byte[] samples;
	byte[] failedSamples;
	// 32 bit + 2 * 8 bit -> 6 byte per pixel

	ReducedNaiveChunk(ViewData viewData, int chunkX, int chunkY, int dimensionSize) {
		super(viewData, chunkX, chunkY, dimensionSize);
		this.values = new float[arrayLength];
		this.samples = new byte[arrayLength];
		this.failedSamples = new byte[arrayLength];
	}
	
	// 64 bit + 2 * 32 bit -> 16 byte per pixel
	
	@Override
	public double getValue(int i) {
		return getValue(i, false);
	}
	
	@Override
	public double getValue(int i, boolean strict) {
		int count = samples[i];
		if (count == 0) {
			if (strict)
				return 0;
			int x = i/dimensionSize;
			int y = i%dimensionSize;
			int upstep = 1;
			while (count == 0) {
				upstep *= 2;
				if (upstep >= dimensionSize)
					return 0;
				x -= x%upstep;
				y -= y%upstep;
				i = (x + upstep-1)*dimensionSize + (y + upstep-1);
				count = samples[i];
				if (count != 0)
					break;
			}
			if (count == 0)
				return 0;
		}
		return values[i] / (samples[i]-failedSamples[i]);
	}
	
	@Override
	public void addSample(int i, double value, int upsample) {
		boolean hadValidValue = samples[i] > failedSamples[i];
		boolean hasValidValue = hadValidValue;
		synchronized (this) {
			if (value < 0)
				failedSamples[i]++;
			else {
				if (samples[i] == 0) //override flags
					values[i] = (float) value;
				else
					values[i] += value;
				hasValidValue = true;
			}
			samples[i]++;
		}
		
		if (!hadValidValue && hasValidValue) {
			int x = i%dimensionSize;
			int y = i/dimensionSize;
			for (ChunkBorderData data : getIndexBorderData(x, y, upsample)) {
				BorderAlignment alignment = data.getAlignment();
				if (alignment.isHorizontal()) {
					data.set(hasValidValue, clampIndex(x-upsample-1), clampIndex(x+upsample));
				} else {
					data.set(hasValidValue, clampIndex(y-upsample-1), clampIndex(y+upsample));
				}
			}
		}
	}
	
	private int clampIndex(int val) {
		return clamp(val, 0, dimensionSize);
	}
	
	private int clamp(int val, int min, int max) {
		return val < min ? min : (val > max ? max : val);
	}

	@Override
	public int getSampleCount(int i) {
		return samples[i];
	}
	
//	static Deflater deflater = new Deflater(0);
//	static Inflater inflater = new Inflater();
//	
//	private void writeObject(ObjectOutputStream oos) throws IOException {
//		oos.defaultWriteObject();
//		
//		if (values == null || samples == null || failedSamples == null)
//			throw new IllegalStateException("can't write object: array null values="+values+" samples="+samples+" failedSamples="+failedSamples);
//		
//		ByteBuffer v = ByteBuffer.allocate(4*values.length);
//		for (int i = 0 ; i < values.length ; i++)
//			v.putFloat(values[i]);
//		byte[] arr = v.array();
////		byte[] values_compressed = Snappy.compress(values);
////		byte[] samples_compressed = Snappy.compress(samples);
////		byte[] failedSamples_compressed = Snappy.compress(failedSamples);
//		deflater.setInput(arr);
//		v = ByteBuffer.allocate(arr.length);
//		System.out.println("bytes: "+deflater.deflate(v.array()));
//		byte[] samples_compressed = samples.clone();
//		byte[] failedSamples_compressed = failedSamples.clone();
//		deflater.setInput(samples);
//		deflater.deflate(samples_compressed);
//		deflater.setInput(failedSamples);
//		deflater.deflate(failedSamples_compressed);
//		oos.writeObject(v.array());
//		oos.writeObject(samples_compressed);
//		oos.writeObject(failedSamples_compressed);
//	}
//	
//	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
//		ois.defaultReadObject();
//		
//		synchronized (this) {
////			values = Snappy.uncompressFloatArray((byte[]) ois.readObject());
////			samples = Snappy.uncompress((byte[]) ois.readObject());
////			failedSamples = Snappy.uncompress((byte[]) ois.readObject());
//			byte[] arr = (byte[])ois.readObject();
//			try {
//				inflater.inflate(arr);
//			} catch (DataFormatException e) {
//				e.printStackTrace();
//			}
//			values = new float[arr.length/4];
//			ByteBuffer buff = ByteBuffer.wrap(arr);
//			for (int i = 0 ; i < values.length ; i++) {
//				values[i] = buff.getFloat();
//			}
//			
//			try {
//				samples = (byte[])ois.readObject();
//				inflater.inflate(samples);
//				failedSamples = (byte[])ois.readObject();
//				inflater.inflate(failedSamples);
//			} catch (DataFormatException e) {
//				e.printStackTrace();
//			}
//		}
//	}

}