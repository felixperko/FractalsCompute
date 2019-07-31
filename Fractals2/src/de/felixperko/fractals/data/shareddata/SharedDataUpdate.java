package de.felixperko.fractals.data.shareddata;

public interface SharedDataUpdate<T> {
	public boolean isSent();
	public void setSent();
}
