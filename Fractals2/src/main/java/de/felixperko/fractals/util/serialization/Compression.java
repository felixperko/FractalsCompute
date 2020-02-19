package de.felixperko.fractals.util.serialization;

import java.io.IOException;
import java.nio.charset.Charset;

public class Compression {
	
	static CompressionFacade instance = null;
	
	static {
		instance = new SnappyDesktopCompression();
	}
	
	public static CompressionFacade getInstance(){
		return instance;
	}

	public static byte[] shuffle(int[] input) throws IOException {
		return instance.shuffle(input);
	}

	public static byte[] shuffle(float[] input) throws IOException {
		return instance.shuffle(input);
	}

	public static byte[] compress(byte[] input) throws IOException {
		return instance.compress(input);
	}

	public static byte[] compress(String input, Charset encoding) throws IOException {
		return instance.compress(input, encoding);
	}

	public static byte[] uncompress(byte[] input) throws IOException {
		return instance.uncompress(input);
	}

	public static String uncompressString(byte[] input, Charset charset) throws IOException {
		return instance.uncompressString(input, charset);
	}

	public static float[] unshuffleFloatArray(byte[] input) throws IOException {
		return instance.unshuffleFloatArray(input);
	}

	public static int[] unshuffleIntArray(byte[] input) throws IOException {
		return instance.unshuffleIntArray(input);
	}
}
