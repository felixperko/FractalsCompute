package de.felixperko.fractals.system.systems.BreadthFirstSystem;

public class DefaultEnabledPixels implements EnabledPixels {
	
	int chunkSize;
	
	public DefaultEnabledPixels(int chunkSize) {
		if (chunkSize <= 0)
			throw new IllegalStateException("chunkSize is invalid");
		this.chunkSize = chunkSize;
	}

	@Override
	public boolean isEnabled(int index) {
		return true;
	}

	@Override
	public int nextEnabled(int currentIndex) {
		currentIndex++;
		if (currentIndex < chunkSize*chunkSize)
			return currentIndex;
		return -1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return obj instanceof DefaultEnabledPixels;
	}

}
