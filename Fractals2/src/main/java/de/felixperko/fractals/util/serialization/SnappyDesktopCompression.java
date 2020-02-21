package de.felixperko.fractals.util.serialization;

import java.io.IOException;
import java.nio.charset.Charset;

import org.xerial.snappy.BitShuffle;
import org.xerial.snappy.Snappy;

public class SnappyDesktopCompression implements CompressionFacade {

	@Override
	public byte[] shuffle(int[] input) throws IOException {
		return BitShuffle.shuffle(input);
	}

	@Override
	public byte[] shuffle(float[] input) throws IOException {
		return BitShuffle.shuffle(input);
	}

	@Override
	public byte[] compress(byte[] input) throws IOException {
		return Snappy.compress(input);
	}

	@Override
	public byte[] compress(String input, Charset encoding) throws IOException {
		return Snappy.compress(input, encoding);
	}

	@Override
	public byte[] uncompress(byte[] input) throws IOException {
		return Snappy.uncompress(input);
	}

	@Override
	public String uncompressString(byte[] input, Charset charset) throws IOException {
		return Snappy.uncompressString(input, charset);
	}

	@Override
	public float[] unshuffleFloatArray(byte[] input) throws IOException {
		return BitShuffle.unshuffleFloatArray(input);
	}

	@Override
	public int[] unshuffleIntArray(byte[] input) throws IOException {
		return BitShuffle.unshuffleIntArray(input);
	}

}
