package de.felixperko.fractals.util.serialization;

import java.io.IOException;
import java.nio.charset.Charset;

public interface CompressionFacade {
	
	public byte[] shuffle(int[] input) throws IOException;
	public byte[] shuffle(float[] input) throws IOException;
	
	public byte[] compress(byte[] input) throws IOException;
	public byte[] compress(String input, Charset encoding) throws IOException;
	
	public byte[] uncompress(byte[] input) throws IOException;
	public String uncompressString(byte[] input, Charset charset) throws IOException;
	
	public float[] unshuffleFloatArray(byte[] input) throws IOException;
	public int[] unshuffleIntArray(byte[] input) throws IOException;
}
